package net.shadowdane.advancedhopper;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricEntryPoint implements ModInitializer {
    public static final String MOD_ID = "advancedhopper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Register blocks, items, block entities, and menus here as they are added.
        LOGGER.info("Advanced Hopper initialized.");
    }
}
