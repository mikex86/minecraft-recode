package me.gommeantilegit.minecraft.world.saveformat.data;

import me.gommeantilegit.minecraft.block.state.BlockStateBase;

/**
 * Represents the pure data of a chunk.
 * @param <BS> the type of block-state to store
 */
public class ChunkData<BS extends BlockStateBase> {

    /**
     * The chunk height
     */
    private final int height;

    /**
     * The three-dimensional blockState array
     */
    private final BS[][][] blockStates;

    public ChunkData(int height, BS[][][] blockStates) {
        this.height = height;
        for (BS[][] blockState : blockStates) {
            assert blockState.length == height;
        }
        this.blockStates = blockStates;
    }

    public int getHeight() {
        return height;
    }

    public BS[][][] getBlockStates() {
        return blockStates;
    }
}
