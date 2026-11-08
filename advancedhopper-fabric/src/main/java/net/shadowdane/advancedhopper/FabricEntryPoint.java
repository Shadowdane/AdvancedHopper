package net.shadowdane.advancedhopper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class FabricEntryPoint implements ModInitializer {
    public static final String MOD_ID = "advancedhopper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AdvancedHopperConfig.initialize();
        ModBlocks.initialize();
        ModItems.initialize();
        ModMenus.initialize();
        LOGGER.info("Advanced Hopper: Completed Initialization.");
    }
}
