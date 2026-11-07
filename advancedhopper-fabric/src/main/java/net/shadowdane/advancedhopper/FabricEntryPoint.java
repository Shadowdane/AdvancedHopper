package net.shadowdane.advancedhopper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class FabricEntryPoint implements ModInitializer {
    public static final String MOD_ID = "advancedhopper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Register blocks, items, block entities, and menus here as they are added.

        ModBlocks.initialize();
        ModItems.initialize();
        LOGGER.info("Advanced Hopper: Completed Initialization.");
    }
}