package me.gommeantilegit.minecraft.utils.palette;

import me.gommeantilegit.minecraft.utils.MathHelper;
import me.gommeantilegit.minecraft.utils.bitarray.BitArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a data structure with indexed types
 *
 * @param <T> the type of data to hold
 */
public class PaletteArray<T> {

    @NotNull
    private final BitArray bitArray;

    @NotNull
    private final IPalette<T> palette;

    public PaletteArray(int size, @NotNull IPalette<T> palette) {
        this.bitArray = new BitArray(size, MathHelper.getNeededBits(palette.getNumKeys()));
        this.palette = palette;
    }

    @Nullable
    public T get(int index) {
        int instanceIndex = this.bitArray.get(index);
        return this.palette.getInstance(instanceIndex);
    }

    public void set(int index, @Nullable T value) {
        int instanceIndex = this.palette.getInstanceIndex(value);
        this.bitArray.set(index, instanceIndex);
    }

    public void clear() {
        this.bitArray.clear();
    }

    public int getByteSize() {
        return this.bitArray.getByteSize();
    }

    public void setData(@NotNull byte[] data) {
        this.bitArray.setData(data);
    }

    @NotNull
    public byte[] getPaletteData() {
        return this.bitArray.getData();
    }
}
