package me.gommeantilegit.minecraft.block.liquid;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.material.Material;
import org.jetbrains.annotations.NotNull;

public abstract class Liquid extends Block {

    public Liquid(int firstPaletteVersion, int id, @NotNull String name, @NotNull Material material) {
        super(firstPaletteVersion, id, name, material);
    }

}
