package net.shadowdane.advancedhopper.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.shadowdane.advancedhopper.AdvancedHopperConfig;
import net.shadowdane.advancedhopper.FabricEntryPoint;

final class AdvancedHopperConfigScreen extends Screen {
    private static final String TEXT = "screen.advancedhopper.config.";
    private static final int ROW_HEIGHT = 23;
    private static final int PANEL_WIDTH = 200;
    private static final int FIELD_WIDTH = 48;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int RED = 0xFFFF5555;

    private final Screen parent;
    private final boolean remoteServer;
    private final List<String> draft = new ArrayList<>();
    private Button saveButton;
    private boolean saveFailed;

    AdvancedHopperConfigScreen(Screen parent) {
        super(Component.translatable(TEXT + "title"));
        this.parent = parent;
        Minecraft client = Minecraft.getInstance();
        remoteServer = client.getConnection() != null && client.getSingleplayerServer() == null;
        if (!remoteServer) {
            AdvancedHopperConfig.readLocalTransferLimits().forEach(limit -> draft.add(limit.toString()));
        }
    }

    @Override
    protected void init() {
        int center = width / 2;
        int top = panelTop();
        int buttonsY = top + 152;
        if (remoteServer) {
            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                    .bounds(center - 50, buttonsY, 100, 20).build());
            return;
        }

        for (int index = 0; index < AdvancedHopperConfig.TIER_COUNT; index++) {
            int tierIndex = index;
            EditBox field = new EditBox(font, center + PANEL_WIDTH / 2 - FIELD_WIDTH, rowY(index),
                    FIELD_WIDTH, 20, tierLabel(index));
            field.setValue(draft.get(index));
            field.setResponder(value -> {
                draft.set(tierIndex, value);
                saveFailed = false;
                saveButton.active = hasValidLimits();
            });
            addRenderableWidget(field);
        }

        saveButton = addRenderableWidget(Button.builder(Component.translatable(TEXT + "save"), button -> save())
                .bounds(center - 100, buttonsY, 96, 20).build());
        saveButton.active = hasValidLimits();
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .bounds(center + 4, buttonsY, 96, 20).build());
    }

    private void save() {
        List<Integer> limits = draft.stream().map(AdvancedHopperConfigScreen::parseLimit).toList();
        try {
            AdvancedHopperConfig.saveLocalTransferLimits(limits);
            onClose();
        } catch (IOException exception) {
            FabricEntryPoint.LOGGER.warn("Could not save the Advanced Hopper config", exception);
            saveFailed = true;
        }
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int center = width / 2;
        int left = center - PANEL_WIDTH / 2;
        int top = panelTop();
        graphics.centeredText(font, title, center, top, WHITE);
        List<Integer> serverLimits = remoteServer ? AdvancedHopperTooltips.syncedTransferLimits() : List.of();
        for (int index = 0; index < AdvancedHopperConfig.TIER_COUNT; index++) {
            int labelY = rowY(index) + 6;
            graphics.text(font, tierLabel(index), left, labelY, WHITE);
            if (remoteServer) {
                Component value = index < serverLimits.size()
                        ? Component.literal(serverLimits.get(index).toString())
                        : Component.translatable(TEXT + "unavailable");
                graphics.centeredText(font, value, center + (PANEL_WIDTH - FIELD_WIDTH) / 2, labelY, GRAY);
            }
        }

        String status = remoteServer ? "server_read_only"
                : saveFailed ? "save_failed"
                : saveButton.active ? "restart_required" : "invalid_limit";
        int statusColor = !remoteServer && (saveFailed || !saveButton.active) ? RED : GRAY;
        graphics.centeredText(font, Component.translatable(TEXT + status), center, top + 137, statusColor);
    }

    private int panelTop() {
        return Math.max(4, (height - 172) / 2);
    }

    private int rowY(int index) {
        return panelTop() + 18 + index * ROW_HEIGHT;
    }

    private static Component tierLabel(int index) {
        return Component.translatable("item.advancedhopper.stack_upgrade_" + (index + 1));
    }

    private boolean hasValidLimits() {
        return draft.stream().mapToInt(AdvancedHopperConfigScreen::parseLimit)
                .allMatch(AdvancedHopperConfig::isValidTransferLimit);
    }

    private static int parseLimit(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
