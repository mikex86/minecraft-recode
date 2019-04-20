package me.gommeantilegit.minecraft.block.state.property.impl;

import me.gommeantilegit.minecraft.block.state.property.BlockStateProperty;
import org.jetbrains.annotations.NotNull;

public class IntBlockStateProperty extends BlockStateProperty<Integer> {

    /**
     * The minimum and maximum values for the given int property
     */
    private int min, max;

    public IntBlockStateProperty(@NotNull String name, int min, int max) {
        super(name, Integer.class);
        this.min = min;
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }
}
