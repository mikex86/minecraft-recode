package me.gommeantilegit.minecraft.world.saveformat.data;

import me.gommeantilegit.minecraft.block.state.BlockStateBase;

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
    private final BlockStateBase[][][] blockStates;

    public ChunkData(int height, BlockStateBase[][][] blockStates) {
        this.height = height;
        for (BlockStateBase[][] blockState : blockStates) {
            assert blockState.length == height;
        }
        this.blockStates = blockStates;
    }

    public int getHeight() {
        return height;
    }

    public BlockStateBase[][][] getBlockStates() {
        return blockStates;
    }
}
