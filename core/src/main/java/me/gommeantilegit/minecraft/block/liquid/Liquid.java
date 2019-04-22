package me.gommeantilegit.minecraft.block.liquid;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.material.Material;
import org.jetbrains.annotations.NotNull;

public abstract class Liquid extends Block {

    public Liquid(@NotNull String name, int id, @NotNull Material material) {
        super(name, id, material);
    }
}
