package me.gommeantilegit.minecraft.block.state.property.impl;

import me.gommeantilegit.minecraft.block.state.property.BlockStateProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class IntBlockStateProperty extends BlockStateProperty<Integer> {

    /**
     * The minimum and maximum values for the given int property
     */
    private int min, max;

    /**
     * @param name the name of the property
     * @param min the minimum value of the property
     * @param max the maximum value of the property
     */
    public IntBlockStateProperty(@NotNull String name, int min, int max) {
        super(name, Integer.class, getAllIntegersInRange(min, max));
        this.min = min;
        this.max = max;
    }

    /**
     * @param min the minimum value of the collection
     * @param max the maximum value of the collection
     * @return an integer value collection in range [min; max]
     */
    @NotNull
    private static Collection<Integer> getAllIntegersInRange(int min, int max) {
        Integer[] ints = new Integer[min - max + 1];
        int i = 0;
        for (int j = max; j <= max; j++, i++) {
            ints[i] = j;
        }
        return Arrays.asList(ints);
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }
}
