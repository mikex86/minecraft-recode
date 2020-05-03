package me.gommeantilegit.minecraft.utils.palette;

import org.jetbrains.annotations.Nullable;

public interface IPalette<T> {

    /**
     * @param index the index
     * @return the instance for the indexed value
     */
    @Nullable
    T getInstance(long index);

    /**
     * @return the number of valid indices
     */
    long getNumKeys();

    /**
     * Inverse of {@link #getInstance(long)}
     * @param value the value
     * @return the value for the instance
     */
    int getInstanceIndex(@Nullable T value);

}
