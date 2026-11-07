package net.shadowdane.advancedhopper.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.shadowdane.advancedhopper.menu.AdvancedHopperMenu;

public final class AdvancedHopperScreen extends AbstractContainerScreen<AdvancedHopperMenu> {
    private static final Identifier HOPPER_TEXTURE =
            Identifier.withDefaultNamespace("textures/gui/container/hopper.png");
    private static final int SLOT_BACKGROUND = 0xFF888888;
    private static final int SLOT_BORDER = 0xFF424242;

    public AdvancedHopperScreen(AdvancedHopperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 133);
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, HOPPER_TEXTURE, leftPos, topPos,
                0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        int x = leftPos + AdvancedHopperMenu.UPGRADE_SLOT_X;
        int y = topPos + AdvancedHopperMenu.UPGRADE_SLOT_Y;
        graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_BACKGROUND);
        for (int offset = 0; offset < 16; offset += 4) {
            graphics.fill(x + offset, y - 1, x + offset + 2, y, SLOT_BORDER);
            graphics.fill(x + offset, y + 16, x + offset + 2, y + 17, SLOT_BORDER);
            graphics.fill(x - 1, y + offset, x, y + offset + 2, SLOT_BORDER);
            graphics.fill(x + 16, y + offset, x + 17, y + offset + 2, SLOT_BORDER);
        }
    }
}
