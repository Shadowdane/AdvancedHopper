package net.shadowdane.advancedhopper;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.shadowdane.advancedhopper.menu.AdvancedHopperMenu;

import static net.shadowdane.advancedhopper.FabricEntryPoint.LOG_PREFIX;

public final class ModMenus {
    public static final MenuType<AdvancedHopperMenu> ADVANCED_HOPPER = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(FabricEntryPoint.MOD_ID, "advanced_hopper"),
            new MenuType<>(AdvancedHopperMenu::new, FeatureFlags.VANILLA_SET)
    );

    private ModMenus() {
    }

    public static void initialize() {
        // Calling this method triggers the menu registration.
        FabricEntryPoint.LOGGER.info(LOG_PREFIX + "Mod Menu support registered.");
    }
}
