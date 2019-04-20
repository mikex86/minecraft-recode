package me.gommeantilegit.minecraft.block.liquid;

import me.gommeantilegit.minecraft.block.BlockBase;
import org.jetbrains.annotations.NotNull;

public abstract class Liquid extends BlockBase {



    public Liquid(@NotNull String name, int id) {
        super(name, id, false, false);
    }
}
