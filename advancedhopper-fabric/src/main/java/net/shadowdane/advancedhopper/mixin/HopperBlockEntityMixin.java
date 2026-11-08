package net.shadowdane.advancedhopper.mixin;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.shadowdane.advancedhopper.AdvancedHopperConfig;
import net.shadowdane.advancedhopper.ModBlocks;
import net.shadowdane.advancedhopper.ModItems;
import net.shadowdane.advancedhopper.UpgradeContainer;
import net.shadowdane.advancedhopper.menu.AdvancedHopperMenu;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends RandomizableContainerBlockEntity {
    @ModifyVariable(method = "setCooldown(I)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    protected HopperBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique
    private static final String advancedhopper$UPGRADE_KEY = "advancedhopper:stack_upgrade";

    @Unique
    private SimpleContainer advancedhopper$upgrade;

    @Shadow
    private long tickedGameTime;


    // Reduce the tick cooldown by 4 ticks.
    private int advancedhopper$reduceCooldown(int ticks) {
        if ((ticks == 8 || ticks == 7)
                && getBlockState().is(ModBlocks.ADVANCED_HOPPER)) {
            return ticks - 4;
        }

        return ticks;
    }

    // Override the hopper GUI to include the stack upgrade slot.
    @Inject(method = "getDefaultName", at = @At("HEAD"), cancellable = true)
    private void advancedhopper$useMenuTitle(CallbackInfoReturnable<Component> callback) {
        if (getBlockState().is(ModBlocks.ADVANCED_HOPPER)) {
            callback.setReturnValue(ModBlocks.ADVANCED_HOPPER.getName());
        }
    }

    private void advancedhopper$openMenu(int containerId, Inventory inventory, CallbackInfoReturnable<AbstractContainerMenu> callback) {
        if (getBlockState().is(ModBlocks.ADVANCED_HOPPER)) {
            callback.setReturnValue(new AdvancedHopperMenu(
                    containerId, inventory, this, advancedhopper$getUpgradeContainer()
            ));
        }
    }

    @Inject(
            method = "createMenu(ILnet/minecraft/world/entity/player/Inventory;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
            at = @At("HEAD"),
            cancellable = true
    )

    @Shadow
    private void setCooldown(int ticks) {
        throw new AssertionError();
    }

    @Shadow
    private boolean isOnCustomCooldown() {
        throw new AssertionError();
    }

    @Shadow
    private static int[] getSlots(Container container, Direction direction) {
        throw new AssertionError();
    }

    @Shadow
    private static @Nullable Container getSourceContainer(Level level, Hopper hopper, BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    @Shadow
    private static @Nullable Container getAttachedContainer(Level level, BlockPos pos, HopperBlockEntity hopper) {
        throw new AssertionError();
    }

    @Shadow
    private static boolean canTakeItemFromContainer(Container into, Container from, ItemStack stack, int slot, Direction direction) {
        throw new AssertionError();
    }

    @Unique
    private int advancedhopper$transferLimit() {
        if (advancedhopper$upgrade == null || !getBlockState().is(ModBlocks.ADVANCED_HOPPER)) {
            return 1;
        }
        int tier = ModItems.stackUpgradeTier(advancedhopper$upgrade.getItem(0));
        return tier == 0 ? 1 : AdvancedHopperConfig.transferLimit(tier);
    }

    @Inject(method = "ejectItems", at = @At("HEAD"), cancellable = true)
    private static void advancedhopper$pushItems(Level level, BlockPos pos, HopperBlockEntity hopper,
                                                  CallbackInfoReturnable<Boolean> callback) {
        HopperBlockEntityMixin self = (HopperBlockEntityMixin) (Object) hopper;
        int limit = self.advancedhopper$transferLimit();
        if (limit == 1) {
            return;
        }

        Direction facing = hopper.getBlockState().getValue(HopperBlock.FACING);
        BlockPos targetPos = pos.relative(facing);
        Container destination = getAttachedContainer(level, pos, hopper);
        Storage<ItemVariant> target = advancedhopper$storage(level, targetPos, facing.getOpposite(), destination);
        if (target == null) {
            callback.setReturnValue(false);
            return;
        }

        HopperBlockEntity receiver = destination instanceof HopperBlockEntity other ? other : null;
        boolean receiverWasEmpty = receiver != null && receiver.isEmpty();
        boolean moved = StorageUtil.move(ContainerStorage.of(hopper, null), target, variant -> true, limit, null) > 0;
        if (moved && receiverWasEmpty) {
            HopperBlockEntityMixin other = (HopperBlockEntityMixin) (Object) receiver;
            if (!other.isOnCustomCooldown()) {
                // Keep vanilla's tick-order compensation when an empty hopper receives items.
                other.setCooldown(other.tickedGameTime >= self.tickedGameTime ? 7 : 8);
            }
        }
        callback.setReturnValue(moved);
    }

    @Inject(method = "suckInItems", at = @At("HEAD"), cancellable = true)
    private static void advancedhopper$pullItems(Level level, Hopper hopper, CallbackInfoReturnable<Boolean> callback) {
        if (!(hopper instanceof HopperBlockEntity blockEntity) || level.isClientSide()) {
            return;
        }
        HopperBlockEntityMixin self = (HopperBlockEntityMixin) (Object) blockEntity;
        int limit = self.advancedhopper$transferLimit();
        if (limit == 1) {
            return;
        }

        BlockPos sourcePos = blockEntity.getBlockPos().above();
        BlockState sourceState = level.getBlockState(sourcePos);
        Container source = getSourceContainer(level, hopper, sourcePos, sourceState);
        Storage<ItemVariant> target = ContainerStorage.of(hopper, null);

        if (source != null && !(source instanceof CompoundContainer) && !sourceState.is(Blocks.COMPOSTER)) {
            callback.setReturnValue(advancedhopper$pullFromContainer(source, hopper, target, limit));
            return;
        }

        Storage<ItemVariant> storage = advancedhopper$storage(level, sourcePos, Direction.DOWN, source);
        if (storage != null) {
            callback.setReturnValue(StorageUtil.move(storage, target, variant -> true, limit, null) > 0);
        } else if (source != null) {
            callback.setReturnValue(false);
        }
        // With no valid item source, the original method will handle the fallback to searching for item entities.
    }

    @Unique
    private static boolean advancedhopper$pullFromContainer(Container source, Hopper hopper, Storage<ItemVariant> target, int limit) {
        ContainerStorage storage = ContainerStorage.of(source, null);
        int remaining = limit;
        try (Transaction transaction = Transaction.openOuter()) {
            for (int slot : getSlots(source, Direction.DOWN)) {
                ItemStack stack = source.getItem(slot);
                if (!stack.isEmpty() && canTakeItemFromContainer(hopper, source, stack, slot, Direction.DOWN)) {
                    remaining -= (int) StorageUtil.move(storage.getSlot(slot), target, variant -> true,
                            remaining, transaction);
                    if (remaining == 0) {
                        break;
                    }
                }
            }
            if (remaining == limit) {
                return false;
            }
            transaction.commit();
            return true;
        }
    }

    @Unique
    private static @Nullable Storage<ItemVariant> advancedhopper$storage(Level level, BlockPos pos, Direction face, @Nullable Container container) {
        // Use Fabric's dedicated adapters for double chests and composters.
        if (container == null || container instanceof CompoundContainer || level.getBlockState(pos).is(Blocks.COMPOSTER)) {
            return ItemStorage.SIDED.find(level, pos, face);
        }
        return ContainerStorage.of(container, face);
    }

    @Unique
    private SimpleContainer advancedhopper$getUpgradeContainer() {
        if (advancedhopper$upgrade == null) {
            advancedhopper$upgrade = new UpgradeContainer(this::setChanged);
        }
        return advancedhopper$upgrade;
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
