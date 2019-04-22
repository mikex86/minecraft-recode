package me.gommeantilegit.minecraft.block.material;

import me.gommeantilegit.minecraft.block.mapcolor.MapColor;
import org.jetbrains.annotations.NotNull;

public class MaterialLiquid extends Material {
    public MaterialLiquid(@NotNull MapColor color) {
        super(color);
        this.setReplaceable();
        this.setNoPushMobility();
    }

    /**
     * Returns if blocks of these materials are liquids.
     */
    public boolean isLiquid() {
        return true;
    }

    /**
     * Returns if this material is considered solid or not
     */
    public boolean blocksMovement() {
        return false;
    }

    /**
     * Returns true if the block is a considered solid. This is true by default.
     */
    public boolean isSolid() {
        return false;
    }
}
