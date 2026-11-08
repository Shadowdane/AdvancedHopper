package net.shadowdane.advancedhopper.network;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.shadowdane.advancedhopper.AdvancedHopperConfig;
import net.shadowdane.advancedhopper.FabricEntryPoint;

public record StackUpgradeLimitsPayload(List<Integer> limits) implements CustomPacketPayload {
    public static final Type<StackUpgradeLimitsPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(FabricEntryPoint.MOD_ID, "stack_upgrade_limits"));
    public static final StreamCodec<ByteBuf, StackUpgradeLimitsPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(AdvancedHopperConfig.TIER_COUNT))
                    .map(StackUpgradeLimitsPayload::new, StackUpgradeLimitsPayload::limits);

    public StackUpgradeLimitsPayload {
        limits = List.copyOf(limits);
        if (limits.size() != AdvancedHopperConfig.TIER_COUNT
                || limits.stream().anyMatch(limit -> limit < 1 || limit > 64)) {
            throw new IllegalArgumentException("Expected five stack upgrade limits between 1 and 64");
        }
    }

    @Override
    public Type<StackUpgradeLimitsPayload> type() {
        return TYPE;
    }
}
