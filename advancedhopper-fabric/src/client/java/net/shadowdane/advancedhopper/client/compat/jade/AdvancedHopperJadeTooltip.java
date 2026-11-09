package net.shadowdane.advancedhopper.client.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HopperBlock;
import net.shadowdane.advancedhopper.ModBlocks;
import net.shadowdane.advancedhopper.ModItems;
import net.shadowdane.advancedhopper.compat.jade.AdvancedHopperJadePlugin;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

public final class AdvancedHopperJadeTooltip implements AdvancedHopperJadePlugin.ClientRegistration,
        IBlockComponentProvider {
    @Override
    public void register(IWailaClientRegistration registration) {
        registration.registerBlockComponent(this, HopperBlock.class);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!accessor.getBlockState().is(ModBlocks.ADVANCED_HOPPER)) {
            return;
        }

        int tier = accessor.getServerData().getIntOr(AdvancedHopperJadePlugin.STACK_UPGRADE_DATA_KEY, 0);
        ItemStack upgrade = ModItems.stackUpgradeForTier(tier);
        if (!upgrade.isEmpty()) {
            tooltip.add(JadeUI.smallItem(upgrade));
            tooltip.append(Component.translatable("tooltip.advancedhopper.jade.installed_upgrade", upgrade.getHoverName()));
        }
    }

    @Override
    public Identifier getUid() {
        return AdvancedHopperJadePlugin.STACK_UPGRADE;
    }
}
