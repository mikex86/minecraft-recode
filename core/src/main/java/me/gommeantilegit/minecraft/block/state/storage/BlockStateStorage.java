package me.gommeantilegit.minecraft.block.state.storage;

import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.block.state.palette.BlockStatePalette;
import me.gommeantilegit.minecraft.utils.palette.PaletteArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class BlockStateStorage {

    @Nullable
    private static BlockStatePalette BLOCK_STATE_PALETTE;

    /**
     * The size of the store
     */
    private final int width, height, depth;

    /**
     * The palette array
     */
    @NotNull
    private final PaletteArray<IBlockState> array;

    /**
     * Called to initialize {@link #BLOCK_STATE_PALETTE}
     *
     * @param blocks the minecraft blocks instance
     */
    public static void initPalette(@NotNull Blocks blocks) {
        BLOCK_STATE_PALETTE = new BlockStatePalette(blocks);
    }

    public BlockStateStorage(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.array = new PaletteArray<>(width * height * depth, getPalette());
    }

    @Nullable
    public IBlockState getBlockState(int x, int y, int z) {
        return this.array.get(index(x, y, z));
    }

    @NotNull
    private BlockStatePalette getPalette() {
        return Objects.requireNonNull(BLOCK_STATE_PALETTE, "BlockStatePalette not initialized!");
    }

    public void set(int relX, int relY, int relZ, @Nullable IBlockState blockState) {
        this.array.set(index(relX, relY, relZ), blockState);
    }

    private int index(int x, int y, int z) {
        return x + width * (y + depth * z);
    }

    public void clear() {
        this.array.clear();
    }

    @NotNull
    public byte[] apply(@NotNull byte[] chunkData) {
        //TODO: THIS IS UGLY
        int byteSize = this.array.getByteSize();
        byte[] data = Arrays.copyOfRange(chunkData, 0, byteSize);
        this.array.setData(data);
        return Arrays.copyOfRange(chunkData, byteSize, chunkData.length);
    }

    @NotNull
    public byte[] getPaletteData() {
        return this.array.getPaletteData();
    }
}
