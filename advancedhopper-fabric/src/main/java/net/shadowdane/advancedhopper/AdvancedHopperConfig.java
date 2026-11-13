package net.shadowdane.advancedhopper;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

import static net.shadowdane.advancedhopper.FabricEntryPoint.LOG_PREFIX;

public final class AdvancedHopperConfig {
    private static final String FILE_NAME = "advanced_hopper.toml";
    private static final String DEFAULT_RESOURCE = "/defaults/" + FILE_NAME;
    private static final String STACK_UPGRADE_KEY_PREFIX = "stack_upgrade_";
    private static final int CONFIG_VERSION = 1;
    public static final int TIER_COUNT = 5;
    public static final int MIN_TRANSFER_LIMIT = 1;
    public static final int MAX_TRANSFER_LIMIT = 64;
    private static volatile List<Integer> activeTransferLimits;

    private AdvancedHopperConfig() {
    }

    public static void initialize() {
        activeTransferLimits = readLocalTransferLimits();
    }

    public static int transferLimit(int tier) {
        return activeTransferLimits.get(tier - 1);
    }

    public static List<Integer> transferLimits() {
        return activeTransferLimits;
    }

    /** Reads the values on disk, including edits saved since startup. */
    public static List<Integer> readLocalTransferLimits() {
        return load(FabricLoader.getInstance().getConfigDir());
    }

    public static List<Integer> defaultTransferLimits() {
        String template = new String(readTemplate(), StandardCharsets.UTF_8);
        return readDefaults(new TomlParser().parse(new StringReader(template)));
    }

    static List<Integer> load(Path configDirectory) {
        byte[] template = readTemplate();
        TomlParser parser = new TomlParser();
        UnmodifiableConfig defaultConfig = parser.parse(
                new StringReader(new String(template, StandardCharsets.UTF_8))
        );
        List<Integer> defaults = readDefaults(defaultConfig);
        Path configFile = configDirectory.resolve(FILE_NAME);

        try {
            Files.createDirectories(configDirectory);
            createConfigIfMissing(configFile, template);

            try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                FabricEntryPoint.LOGGER.info(LOG_PREFIX + "Loaded config from: {}", configFile);
                return readLimits(parser.parse(reader), defaults);
            }
        } catch (IOException | ParsingException exception) {
            FabricEntryPoint.LOGGER.warn(LOG_PREFIX + "Could not read or create {}. Using bundled defaults: {}",
                    configFile, exception.getMessage());
            return defaults;
        }
    }

    private static byte[] readTemplate() {
        try (InputStream stream = AdvancedHopperConfig.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Missing bundled config: " + DEFAULT_RESOURCE);
            }
            return stream.readAllBytes();
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read bundled config", exception);
        }
    }

    private static List<Integer> readDefaults(UnmodifiableConfig config) {
        Object section = config.getRaw("StackUpgrade");
        List<Integer> defaults = new ArrayList<>(TIER_COUNT);
        for (int tier = 1; tier <= TIER_COUNT; tier++) {
            String key = STACK_UPGRADE_KEY_PREFIX + tier;
            long value = integerValue(getValue(section, key));
            if (!isValidTransferLimit(value)) {
                throw new IllegalStateException("Invalid bundled default for StackUpgrade." + key);
            }
            defaults.add((int) value);
        }
        return List.copyOf(defaults);
    }

    private static void createConfigIfMissing(Path configFile, byte[] template) throws IOException {
        try {
            Files.write(configFile, template, StandardOpenOption.CREATE_NEW);
        } catch (FileAlreadyExistsException ignored) {
            // Keep existing config if it exists
        }
    }

    private static List<Integer> readLimits(UnmodifiableConfig config, List<Integer> defaults) {
        Object version = getValue(config.getRaw("General"), "configVersion");
        if (version != null && integerValue(version) != CONFIG_VERSION) {
            FabricEntryPoint.LOGGER.warn(LOG_PREFIX + "Unsupported {} configVersion ({}). Using bundled defaults.",
                    FILE_NAME, version);
            return defaults;
        }

        Object section = config.getRaw("StackUpgrade");
        if (section != null && !(section instanceof UnmodifiableConfig)) {
            FabricEntryPoint.LOGGER.warn(LOG_PREFIX + "StackUpgrade must be a TOML table in {}. Using bundled defaults.",
                    FILE_NAME);
            return defaults;
        }

        List<Integer> limits = new ArrayList<>(TIER_COUNT);
        for (int tier = 1; tier <= TIER_COUNT; tier++) {
            String key = STACK_UPGRADE_KEY_PREFIX + tier;
            Object configured = getValue(section, key);
            int defaultLimit = defaults.get(tier - 1);
            long value = integerValue(configured);
            if (configured == null) {
                limits.add(defaultLimit);
            } else if (!isValidTransferLimit(value)) {
                FabricEntryPoint.LOGGER.warn(LOG_PREFIX + "StackUpgrade.{} in {} must be an integer from 1 to 64. Using {}.",
                        key, FILE_NAME, defaultLimit);
                limits.add(defaultLimit);
            } else {
                if (value == 1) {
                    FabricEntryPoint.LOGGER.warn(LOG_PREFIX + "StackUpgrade.{} is set to 1 in {}. This tier gives no transfer boost.",
                            key, FILE_NAME);
                }
                limits.add((int) value);
            }
        }
        return List.copyOf(limits);
    }

    // Saves the settings and makes them active after a successful write.
    public static void saveLocalTransferLimits(List<Integer> limits) throws IOException {
        saveTransferLimits(FabricLoader.getInstance().getConfigDir(), limits);
    }

    static void saveTransferLimits(Path configDirectory, List<Integer> limits) throws IOException {
        limits = List.copyOf(limits);
        if (limits.size() != TIER_COUNT || limits.stream().anyMatch(limit -> !isValidTransferLimit(limit))) {
            throw new IllegalArgumentException("Expected five transfer limits from 1 to 64");
        }

        Path configFile = configDirectory.resolve(FILE_NAME);
        CommentedConfig config;
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            config = new TomlParser().parse(reader);
        } catch (ParsingException exception) {
            throw new IOException("Could not parse " + configFile, exception);
        }

        Object version = getValue(config.getRaw("General"), "configVersion");
        if (version != null && integerValue(version) != CONFIG_VERSION) {
            throw new IOException("Unsupported configVersion in " + configFile + ": " + version);
        }
        Object section = config.getRaw("StackUpgrade");
        if (section != null && !(section instanceof UnmodifiableConfig)) {
            throw new IOException("StackUpgrade must be a TOML table in " + configFile);
        }

        for (int tier = 1; tier <= TIER_COUNT; tier++) {
            config.set(List.of("StackUpgrade", STACK_UPGRADE_KEY_PREFIX + tier), limits.get(tier - 1));
        }

        // Finish writing the new TOML before replacing the existing file.
        Path tempFile = Files.createTempFile(configDirectory, FILE_NAME, ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                new TomlWriter().write(config, writer);
            } catch (WritingException exception) {
                throw new IOException("Could not write " + configFile, exception);
            }
            Files.move(tempFile, configFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempFile);
        }
        activeTransferLimits = limits;
    }

    public static boolean isValidTransferLimit(long value) {
        return value >= MIN_TRANSFER_LIMIT && value <= MAX_TRANSFER_LIMIT;
    }

    private static Object getValue(Object section, String key) {
        return section instanceof UnmodifiableConfig table ? table.getRaw(key) : null;
    }

    private static long integerValue(Object value) {
        return value instanceof Integer || value instanceof Long ? ((Number) value).longValue() : 0;
    }
}
