package me.gommeantilegit.minecraft.utils.collections;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;

public class SortedArrayList<T> extends ArrayList<T> {

    /**
     * The comparator sorting the list
     */
    @NotNull
    private final Comparator<T> comparator;

    public SortedArrayList(@NotNull Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(T t) {
        boolean state = super.add(t);
        if (state) {
            sort(comparator);
        }
        return state;
    }
}
