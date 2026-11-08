package net.shadowdane.advancedhopper.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.shadowdane.advancedhopper.ModItems;
import net.shadowdane.advancedhopper.ModMenus;

public final class AdvancedHopperMenu extends AbstractContainerMenu {
    private static final int UPGRADE_SLOT_X = 13;
    private static final int UPGRADE_SLOT_Y = 20;

    private static final int HOPPER_SLOTS = HopperBlockEntity.HOPPER_CONTAINER_SIZE;
    private static final int UPGRADE_SLOT = HOPPER_SLOTS;
    private static final int PLAYER_START = UPGRADE_SLOT + 1;

    private final Container hopper;

    public AdvancedHopperMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(HOPPER_SLOTS), new SimpleContainer(1));
    }

    public AdvancedHopperMenu(int containerId, Inventory inventory, Container hopper, Container upgrade) {
        super(ModMenus.ADVANCED_HOPPER, containerId);
        checkContainerSize(hopper, HOPPER_SLOTS);
        checkContainerSize(upgrade, 1);
        this.hopper = hopper;
        hopper.startOpen(inventory.player);

        for (int index = 0; index < HOPPER_SLOTS; index++) {
            addSlot(new Slot(hopper, index, 44 + index * 18, 20));
        }
        addSlot(new Slot(upgrade, 0, UPGRADE_SLOT_X, UPGRADE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ModItems.stackUpgradeTier(stack) > 0;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addStandardInventorySlots(inventory, 8, 51);
    }

    public Slot getUpgradeSlot() {
        return getSlot(UPGRADE_SLOT);
    }

    @Override
    public boolean stillValid(Player player) {
        return hopper.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (slotIndex < PLAYER_START) {
            moved = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        } else if (ModItems.stackUpgradeTier(stack) > 0) {
            moved = moveItemStackTo(stack, UPGRADE_SLOT, PLAYER_START, false);
        } else {
            moved = moveItemStackTo(stack, 0, HOPPER_SLOTS, false);
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        hopper.stopOpen(player);
    }
}
