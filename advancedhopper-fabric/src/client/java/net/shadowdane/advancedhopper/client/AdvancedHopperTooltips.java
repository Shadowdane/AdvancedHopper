package net.shadowdane.advancedhopper.client;

import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.shadowdane.advancedhopper.ModBlocks;
import net.shadowdane.advancedhopper.ModItems;
import net.shadowdane.advancedhopper.network.StackUpgradeLimitsPayload;
import org.jspecify.annotations.Nullable;

final class AdvancedHopperTooltips {
    private static volatile @Nullable StackUpgradeLimitsPayload serverLimits;

    private AdvancedHopperTooltips() {
    }

    static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(StackUpgradeLimitsPayload.TYPE,
                (payload, context) -> serverLimits = payload);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> serverLimits = null);
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> appendTooltip(stack, lines));
    }

    private static void appendTooltip(ItemStack stack, List<Component> lines) {
        if (lines.isEmpty()) {
            return;
        }

        int tier = ModItems.stackUpgradeTier(stack);
        MutableComponent description;
        if (tier > 0) {
            StackUpgradeLimitsPayload limits = serverLimits;
            description = limits == null
                    ? Component.translatable("tooltip.advancedhopper.stack_upgrade")
                    : Component.translatable("tooltip.advancedhopper.stack_upgrade_limit",
                            limits.limits().get(tier - 1));
        } else if (stack.is(ModBlocks.ADVANCED_HOPPER.asItem())) {
            description = Component.translatable("tooltip.advancedhopper.advanced_hopper");
        } else if (stack.is(ModItems.UPGRADE_BASE)) {
            description = Component.translatable("tooltip.advancedhopper.upgrade_base");
        } else {
            return;
        }

        lines.add(1, description.withStyle(ChatFormatting.GRAY));
    }
}
