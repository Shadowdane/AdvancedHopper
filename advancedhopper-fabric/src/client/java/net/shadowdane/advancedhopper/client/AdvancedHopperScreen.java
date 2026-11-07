package net.shadowdane.advancedhopper.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.shadowdane.advancedhopper.menu.AdvancedHopperMenu;

public final class AdvancedHopperScreen extends AbstractContainerScreen<AdvancedHopperMenu> {
    private static final Identifier HOPPER_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/hopper.png");
    private static final int UPGRADE_OUTLINE_COLOR = 0xFF424242;

    public AdvancedHopperScreen(AdvancedHopperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 133);
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, HOPPER_TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        Slot upgradeSlot = menu.getUpgradeSlot();
        int x = leftPos + upgradeSlot.x;
        int y = topPos + upgradeSlot.y;
        // Reuse the existing hopper slot frame
        graphics.blit(RenderPipelines.GUI_TEXTURED, HOPPER_TEXTURE, x - 1, y - 1,
                43.0F, 19.0F, 18, 18, 256, 256);

        if (!upgradeSlot.hasItem()) {
            // Draw dotted outline for Stack Upgrade Slot
            for (int offset = 2; offset < 14; offset += 5) {
                graphics.fill(x + offset, y + 1, x + offset + 2, y + 2, UPGRADE_OUTLINE_COLOR);
                graphics.fill(x + offset, y + 14, x + offset + 2, y + 15, UPGRADE_OUTLINE_COLOR);
                graphics.fill(x + 1, y + offset, x + 2, y + offset + 2, UPGRADE_OUTLINE_COLOR);
                graphics.fill(x + 14, y + offset, x + 15, y + offset + 2, UPGRADE_OUTLINE_COLOR);
            }
        }
    }
}
