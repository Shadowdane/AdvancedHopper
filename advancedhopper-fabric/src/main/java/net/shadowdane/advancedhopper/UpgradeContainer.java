package net.shadowdane.advancedhopper;

import net.minecraft.world.SimpleContainer;

public final class UpgradeContainer extends SimpleContainer {
    private final Runnable onChanged;

    public UpgradeContainer(Runnable onChanged) {
        super(1);
        this.onChanged = onChanged;
    }

    @Override
    public void setChanged() {
        onChanged.run();
    }
}
