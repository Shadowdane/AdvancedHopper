package net.shadowdane.advancedhopper.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.shadowdane.advancedhopper.network.StackUpgradeLimitsPayload;
import net.shadowdane.advancedhopper.AdvancedHopperConfig;
import net.shadowdane.advancedhopper.FabricEntryPoint;

import static net.shadowdane.advancedhopper.FabricEntryPoint.LOG_PREFIX;

final class AdvancedHopperConfigScreen extends Screen {
    private static final String TEXT = "screen.advancedhopper.config.";
    private static final int ROW_HEIGHT = 24;
    private static final int HEADER_HEIGHT = 56;
    private static final int FOOTER_HEIGHT = 56;
    private static final int PANEL_WIDTH = 300;
    private static final int FIELD_WIDTH = 48;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int RED = 0xFFFF5555;

    private final Screen parent;
    private final boolean remoteServer;
    private final List<Integer> defaults = AdvancedHopperConfig.defaultTransferLimits();
    private final List<String> draft = new ArrayList<>();
    private SettingsList settingsList;
    private Button saveButton;
    private boolean saveFailed;

    AdvancedHopperConfigScreen(Screen parent) {
        super(Component.translatable(TEXT + "title"));
        this.parent = parent;
        Minecraft client = Minecraft.getInstance();
        remoteServer = client.getConnection() != null && client.getSingleplayerServer() == null;
        if (!remoteServer) {
            AdvancedHopperConfig.readLocalTransferLimits().forEach(limit -> draft.add(limit.toString()));
        }
    }

    @Override
    protected void init() {
        int center = width / 2;
        int footerTop = height - FOOTER_HEIGHT;
        int gap = 4;
        int footerContentHeight = font.lineHeight + gap + 20;
        int footerContentTop = footerTop + (FOOTER_HEIGHT - footerContentHeight) / 2;
        int buttonsY = footerContentTop + font.lineHeight + gap;
        double scroll = settingsList == null ? 0 : settingsList.scrollAmount();
        settingsList = addRenderableWidget(new SettingsList());
        settingsList.setScrollAmount(scroll);
        if (remoteServer) {
            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                    .bounds(center - 50, buttonsY, 100, 20).build());
            return;
        }

        saveButton = addRenderableWidget(Button.builder(Component.translatable(TEXT + "save"), button -> save())
                .bounds(center - 100, buttonsY, 96, 20).build());
        saveButton.active = hasValidLimits();
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .bounds(center + 4, buttonsY, 96, 20).build());
    }  

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int footerTop = height - FOOTER_HEIGHT;
        int titleY = (HEADER_HEIGHT - font.lineHeight) / 2;
        graphics.fill(0, 0, width, HEADER_HEIGHT, 0x55000000);
        graphics.fill(0, footerTop, width, height, 0x55000000);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, titleY, WHITE);
        graphics.fill(0, HEADER_HEIGHT - 1, width, HEADER_HEIGHT, 0xFF666666);
        graphics.fill(0, footerTop, width, footerTop + 1, 0xFF666666);

        String status = remoteServer ? "server_read_only"
                : saveFailed ? "save_failed"
                : saveButton.active ? "apply_on_save" : "invalid_limit";
        int statusColor = !remoteServer && (saveFailed || !saveButton.active) ? RED : GRAY;
        graphics.centeredText(font, Component.translatable(TEXT + status), width / 2, footerTop + 7, statusColor);
    }

    private void save() {
        if (remoteServer || !hasValidLimits()) {
            return;
        }
        List<Integer> limits = draft.stream().map(AdvancedHopperConfigScreen::parseLimit).toList();
        try {
            AdvancedHopperConfig.saveLocalTransferLimits(limits);
            MinecraftServer server = minecraft.getSingleplayerServer();
            if (server != null) {
                server.execute(() -> {
                    StackUpgradeLimitsPayload payload = new StackUpgradeLimitsPayload(AdvancedHopperConfig.transferLimits());
                    for (var player : server.getPlayerList().getPlayers()) {
                        if (ServerPlayNetworking.canSend(player, StackUpgradeLimitsPayload.TYPE)) {
                            ServerPlayNetworking.send(player, payload);
                        }
                    }
                });
            }
            onClose();
        } catch (IOException exception) {
            FabricEntryPoint.LOGGER.warn(LOG_PREFIX + "Could not save the Advanced Hopper config", exception);
            saveFailed = true;
        }
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }


    private final class SettingsList extends ContainerObjectSelectionList<ConfigEntry> {
        private SettingsList() {
            super(AdvancedHopperConfigScreen.this.minecraft, AdvancedHopperConfigScreen.this.width,
                    Math.max(1, AdvancedHopperConfigScreen.this.height - HEADER_HEIGHT - FOOTER_HEIGHT),
                    HEADER_HEIGHT, ROW_HEIGHT);
            centerListVertically = false;
            addEntry(new SectionEntry());
            for (int index = 0; index < AdvancedHopperConfig.TIER_COUNT; index++) {
                addEntry(new TierEntry(index));
            }
        }

        @Override
        public int getRowWidth() {
            return Math.min(PANEL_WIDTH, width - 20);
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor graphics) {
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor graphics) {
            // The screen draws the full-width header and footer separators.
        }
    }

    private abstract class ConfigEntry extends ContainerObjectSelectionList.Entry<ConfigEntry> {
        protected final List<AbstractWidget> widgets = new ArrayList<>();

        @Override
        public List<? extends GuiEventListener> children() {
            return widgets;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return widgets;
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            positionWidgets();
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            positionWidgets();
        }

        @Override
        public void setWidth(int width) {
            super.setWidth(width);
            positionWidgets();
        }

        private void positionWidgets() {
            for (int index = 0; index < widgets.size(); index++) {
                AbstractWidget widget = widgets.get(index);
                widget.setX(getContentRight() - 112 + index * 56);
                widget.setY(getContentY());
            }
        }
    }

    private final class SectionEntry extends ConfigEntry {
        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   boolean hovered, float delta) {
            graphics.centeredText(font, Component.translatable(TEXT + "section"),
                    getContentXMiddle(), getContentY() + 6, WHITE);
        }
    }

    private final class TierEntry extends ConfigEntry {
        private final int tierIndex;

        private TierEntry(int tierIndex) {
            this.tierIndex = tierIndex;
            if (remoteServer) {
                return;
            }
            EditBox field = new EditBox(font, 0, 0, FIELD_WIDTH, 20, tierLabel(tierIndex));
            field.setValue(draft.get(tierIndex));
            field.setTooltip(Tooltip.create(Component.translatable(
                    TEXT + "stack_upgrade_" + (tierIndex + 1) + ".tooltip")));
            Button reset = Button.builder(Component.translatable(TEXT + "reset"),
                    button -> field.setValue(defaults.get(tierIndex).toString()))
                    .bounds(0, 0, 56, 20).build();
            reset.active = !draft.get(tierIndex).equals(defaults.get(tierIndex).toString());
            field.setResponder(value -> {
                draft.set(tierIndex, value);
                saveFailed = false;
                reset.active = !value.equals(defaults.get(tierIndex).toString());
                saveButton.active = hasValidLimits();
            });
            widgets.add(field);
            widgets.add(reset);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   boolean hovered, float delta) {
            graphics.text(font, tierLabel(tierIndex), getContentX(), getContentY() + 6, WHITE);
            if (remoteServer) {
                List<Integer> limits = AdvancedHopperTooltips.syncedTransferLimits();
                Component value = tierIndex < limits.size()
                        ? Component.literal(limits.get(tierIndex).toString())
                        : Component.translatable(TEXT + "unavailable");
                graphics.centeredText(font, value, getContentRight() - 88, getContentY() + 6, GRAY);
            }
            for (AbstractWidget widget : widgets) {
                widget.extractRenderState(graphics, mouseX, mouseY, delta);
            }
        }
    }

    private static Component tierLabel(int index) {
        return Component.translatable("item.advancedhopper.stack_upgrade_" + (index + 1));
    }

    private boolean hasValidLimits() {
        return draft.stream().mapToInt(AdvancedHopperConfigScreen::parseLimit)
                .allMatch(AdvancedHopperConfig::isValidTransferLimit);
    }

    private static int parseLimit(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
