package me.gommeantilegit.minecraft.block.material;

import me.gommeantilegit.minecraft.block.mapcolor.MapColor;
import org.jetbrains.annotations.NotNull;

public class MaterialTransparent extends Material
{
    public MaterialTransparent(@NotNull MapColor color)
    {
        super(color);
        this.setReplaceable();
    }

    /**
     * Returns true if the block is a considered solid. This is true by default.
     */
    public boolean isSolid()
    {
        return false;
    }

    /**
     * Will prevent grass from growing on dirt underneath and kill any grass below it if it returns true
     */
    public boolean blocksLight()
    {
        return false;
    }

    /**
     * Returns if this material is considered solid or not
     */
    public boolean blocksMovement()
    {
        return false;
    }
}