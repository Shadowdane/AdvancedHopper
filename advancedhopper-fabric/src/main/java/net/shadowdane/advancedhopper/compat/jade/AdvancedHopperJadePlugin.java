package net.shadowdane.advancedhopper.compat.jade;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.shadowdane.advancedhopper.AdvancedHopperConfig;
import net.shadowdane.advancedhopper.FabricEntryPoint;
import net.shadowdane.advancedhopper.HopperUpgradeAccess;
import net.shadowdane.advancedhopper.ModBlocks;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;

import static net.shadowdane.advancedhopper.FabricEntryPoint.LOG_PREFIX;

public final class AdvancedHopperJadePlugin implements IWailaPlugin, IServerDataProvider<BlockAccessor> {
    public static final Identifier STACK_UPGRADE = Identifier.fromNamespaceAndPath(
            FabricEntryPoint.MOD_ID, "stack_upgrade");
    public static final String STACK_UPGRADE_DATA_KEY = STACK_UPGRADE.toString();
    public static final String STACK_LIMIT_DATA_KEY = FabricEntryPoint.MOD_ID + ":stack_limit";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(this, HopperBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Load the tooltip provider only on the client, where Jade's GUI API is available.
        FabricLoader.getInstance().invokeEntrypoints("advancedhopper:jade_client", ClientRegistration.class, entrypoint -> entrypoint.register(registration));
        FabricEntryPoint.LOGGER.info(LOG_PREFIX + "Jade Plugin registered.");
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockState().is(ModBlocks.ADVANCED_HOPPER);
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockState().is(ModBlocks.ADVANCED_HOPPER)
                && accessor.getBlockEntity() instanceof HopperUpgradeAccess hopper) {
            int tier = hopper.advancedhopper$getStackUpgradeTier();
            if (tier > 0) {
                data.putInt(STACK_UPGRADE_DATA_KEY, tier);
                data.putInt(STACK_LIMIT_DATA_KEY, AdvancedHopperConfig.transferLimit(tier));
            }
        }
    }

    @Override
    public Identifier getUid() {
        return STACK_UPGRADE;
    }

    public interface ClientRegistration {
        void register(IWailaClientRegistration registration);
    }
}
