package net.shadowdane.advancedhopper.mixin;

import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.shadowdane.advancedhopper.ModBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @ModifyVariable(
            method = "setCooldown(I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private int advancedhopper$reduceCooldown(int ticks) {
        HopperBlockEntity hopper = (HopperBlockEntity) (Object) this;

        if ((ticks == 8 || ticks == 7)
                && hopper.getBlockState().is(ModBlocks.ADVANCED_HOPPER)) {
            return ticks - 4;
        }

        return ticks;
    }
}