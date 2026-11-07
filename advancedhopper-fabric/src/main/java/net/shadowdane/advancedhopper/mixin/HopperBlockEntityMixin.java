package net.shadowdane.advancedhopper.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.shadowdane.advancedhopper.ModBlocks;
import net.shadowdane.advancedhopper.UpgradeContainer;
import net.shadowdane.advancedhopper.menu.AdvancedHopperMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends RandomizableContainerBlockEntity {
    @Unique
    private static final String advancedhopper$UPGRADE_KEY = "advancedhopper:stack_upgrade";

    @Unique
    private SimpleContainer advancedhopper$upgrade;

    protected HopperBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @ModifyVariable(
            method = "setCooldown(I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private int advancedhopper$reduceCooldown(int ticks) {
        if ((ticks == 8 || ticks == 7)
                && getBlockState().is(ModBlocks.ADVANCED_HOPPER)) {
            return ticks - 4;
        }

        return ticks;
    }

    @Unique
    private SimpleContainer advancedhopper$getUpgradeContainer() {
        if (advancedhopper$upgrade == null) {
            advancedhopper$upgrade = new UpgradeContainer(this::setChanged);
        }
        return advancedhopper$upgrade;
    }

    @Inject(
            method = "createMenu(ILnet/minecraft/world/entity/player/Inventory;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void advancedhopper$openMenu(int containerId, Inventory inventory,
                                          CallbackInfoReturnable<AbstractContainerMenu> callback) {
        if (getBlockState().is(ModBlocks.ADVANCED_HOPPER)) {
            callback.setReturnValue(new AdvancedHopperMenu(
                    containerId, inventory, this, advancedhopper$getUpgradeContainer()
            ));
        }
    }

    @Inject(method = "getDefaultName", at = @At("HEAD"), cancellable = true)
    private void advancedhopper$useMenuTitle(CallbackInfoReturnable<Component> callback) {
        if (getBlockState().is(ModBlocks.ADVANCED_HOPPER)) {
            callback.setReturnValue(ModBlocks.ADVANCED_HOPPER.getName());
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void advancedhopper$loadUpgrade(ValueInput input, CallbackInfo callback) {
        if (getBlockState().is(ModBlocks.ADVANCED_HOPPER)) {
            ItemStack upgrade = input.read(advancedhopper$UPGRADE_KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY);
            if (advancedhopper$upgrade != null || !upgrade.isEmpty()) {
                advancedhopper$getUpgradeContainer().setItem(0, upgrade);
            }
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void advancedhopper$saveUpgrade(ValueOutput output, CallbackInfo callback) {
        if (advancedhopper$upgrade != null && !advancedhopper$upgrade.isEmpty()) {
            output.store(advancedhopper$UPGRADE_KEY, ItemStack.CODEC, advancedhopper$upgrade.getItem(0));
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && advancedhopper$upgrade != null) {
            Containers.dropContents(level, pos, advancedhopper$upgrade);
        }
    }
}
