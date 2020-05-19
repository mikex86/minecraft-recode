package me.gommeantilegit.minecraft.utils.palette;

import me.gommeantilegit.minecraft.utils.MathHelper;
import org.jetbrains.annotations.Nullable;

public interface IPalette<T> {

    /**
     * @param index the index
     * @return the instance for the indexed value
     */
    @Nullable
    T getInstance(int index);

    /**
     * @return the number of valid indices
     */
    int getNumKeys();

    /**
     * Inverse of {@link #getInstance(int)}
     *
     * @param value the value
     * @return the value for the instance
     */
    int getInstanceIndex(@Nullable T value);

    /**
     * The number of bits the highest key index of the palette
     */
    default int getNeededKeyBits() {
        return MathHelper.getNeededBits(getNumKeys() - 1);
    }
}
