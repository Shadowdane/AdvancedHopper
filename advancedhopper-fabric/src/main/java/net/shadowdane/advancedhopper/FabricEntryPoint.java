package net.shadowdane.advancedhopper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.shadowdane.advancedhopper.network.StackUpgradeLimitsPayload;

public class FabricEntryPoint implements ModInitializer {
    public static final String MOD_ID = "advancedhopper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AdvancedHopperConfig.initialize();
        ModBlocks.initialize();
        ModItems.initialize();
        ModMenus.initialize();

        PayloadTypeRegistry.clientboundPlay().register(
                StackUpgradeLimitsPayload.TYPE, StackUpgradeLimitsPayload.STREAM_CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler.player, StackUpgradeLimitsPayload.TYPE)) {
                sender.sendPacket(new StackUpgradeLimitsPayload(AdvancedHopperConfig.transferLimits()));
            }
        });
        LOGGER.info("Advanced Hopper: Completed Initialization.");
    }
}
