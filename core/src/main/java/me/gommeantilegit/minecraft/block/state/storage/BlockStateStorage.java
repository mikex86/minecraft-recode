package me.gommeantilegit.minecraft.block.state.storage;

import me.gommeantilegit.minecraft.block.access.IWritableBlockStateAccess;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.block.state.palette.IBlockStatePalette;
import me.gommeantilegit.minecraft.utils.palette.PaletteArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockStateStorage implements IWritableBlockStateAccess {

    /**
     * The size of the store
     */
    private final int width, height, depth;

    /**
     * The palette array
     */
    @NotNull
    private final PaletteArray<IBlockState> array;

    public BlockStateStorage(int width, int height, int depth, @NotNull IBlockStatePalette palette) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.array = new PaletteArray<>(width * height * depth, palette);
    }

    public BlockStateStorage(int width, int height, int depth, @NotNull PaletteArray<IBlockState> paletteArray) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.array = paletteArray;
    }

    @Nullable
    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        return this.array.get(index(x, y, z));
    }

    @Override
    public void set(int relX, int relY, int relZ, @Nullable IBlockState blockState) {
        this.array.set(index(relX, relY, relZ), blockState);
    }

    // todo: investigate if this is safe
    private int index(int x, int y, int z) {
        return x + width * (y + depth * z);
    }

    public void clear() {
        this.array.clear();
    }

    public void apply(@NotNull byte[] data) {
        int byteSize = this.array.getByteSize();
        if (byteSize != data.length)
            throw new IllegalArgumentException("Cannot apply block storage data of invalid length. Required: " + byteSize + ", Received: " + data.length);
        this.array.setData(data);
    }

    @NotNull
    public byte[] getPaletteData() {
        return this.array.getPaletteData();
    }

    public void delete() {
        this.array.delete();
    }

    @NotNull
    public BlockStateStorage copy() {
        return new BlockStateStorage(this.width, this.height, this.depth, this.array.copy());
    }
}
