package me.gommeantilegit.minecraft.world.saveformat.data;

import me.gommeantilegit.minecraft.block.state.BlockState;

/**
 * Represents the pure data of a chunk.
 */
public class ChunkData {

    /**
     * The chunk height
     */
    private final int height;

    /**
     * The three-dimensional blockState array
     */
    private final BlockState[][][] blockStates;

    public ChunkData(int height, BlockState[][][] blockStates) {
        this.height = height;
        for (BlockState[][] blockState : blockStates) {
            assert blockState.length == height;
        }
        this.blockStates = blockStates;
    }

    public int getHeight() {
        return height;
    }

    public BlockState[][][] getBlockStates() {
        return blockStates;
    }
}
