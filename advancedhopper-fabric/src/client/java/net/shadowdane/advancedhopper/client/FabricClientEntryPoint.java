package net.shadowdane.advancedhopper.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.shadowdane.advancedhopper.ModMenus;

public class FabricClientEntryPoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenus.ADVANCED_HOPPER, AdvancedHopperScreen::new);
    }
}
