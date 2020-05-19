package me.gommeantilegit.minecraft.block.state.palette;

import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.utils.bitarray.BitArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a block state palette that stores a limited collection of occurring block states created from a bit-array which uses indices retrieved from a {@link GlobalBlockStatePalette}
 */
public class IndexedBlockStatePalette implements IBlockStatePalette {

    /**
     * The list of individual block states in the palette
     */
    @NotNull
    private final List<IBlockState> blockStates;

    public IndexedBlockStatePalette(@NotNull List<IBlockState> blockStates) {
        this.blockStates = blockStates;
    }

    public IndexedBlockStatePalette(@NotNull Set<IBlockState> blockStates) {
        this(new ArrayList<>(blockStates));
    }

    @NotNull
    public static IndexedBlockStatePalette getPalette(@NotNull Blocks blocks, int paletteVersion, @NotNull byte[] paletteBacking, int elementBits) {
        BitArray bitArray = new BitArray(paletteBacking, elementBits);
        List<IBlockState> blockStates = new ArrayList<>();
        List<IBlockState> allBlockStates = blocks.getPossibleBlockStates(paletteVersion);
        for (int i = 0; i < bitArray.getNumElements(); i++) {
            int blockStateIndex = bitArray.get(i);
            IBlockState state = allBlockStates.get(blockStateIndex);
            blockStates.add(state);
        }
        return new IndexedBlockStatePalette(blockStates);
    }

    @Nullable
    @Override
    public IBlockState getInstance(int index) {
        return this.blockStates.get(index);
    }

    @Override
    public int getNumKeys() {
        return this.blockStates.size();
    }

    @Override
    public int getInstanceIndex(@Nullable IBlockState value) {
        return this.blockStates.indexOf(value);
    }

    @NotNull
    public int[] getInstanceIndices(@NotNull IBlockStatePalette globalPalette) {
        int[] indices = new int[this.blockStates.size()];
        for (int i = 0; i < this.blockStates.size(); i++) {
            indices[i] = globalPalette.getInstanceIndex(this.blockStates.get(i));
        }
        return indices;
    }
}
