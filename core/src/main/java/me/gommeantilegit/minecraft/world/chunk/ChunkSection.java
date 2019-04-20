package me.gommeantilegit.minecraft.world.chunk;

import org.jetbrains.annotations.NotNull;

public class ChunkSection {

    /**
     * X, Y, Z size of a Chunk Section
     */
    public static final int CHUNK_SECTION_SIZE = ChunkBase.CHUNK_SIZE;

    /**
     * The parent chunk
     */
    @NotNull
    private final ChunkBase parentChunk;

    /**
     * The y coordinate where the chunk section starts. Must be a multiple of {@link #CHUNK_SECTION_SIZE}
     */
    private final int startHeight;

    public ChunkSection(@NotNull ChunkBase parentChunk, int startHeight) {
        this.parentChunk = parentChunk;
        this.startHeight = startHeight;
    }

    /**
     * @return true if the chunk section is fully empty meaning that every block is air in the given section of the {@link #parentChunk}
     */
    public boolean isEmpty() {
        for (int xo = 0; xo < CHUNK_SECTION_SIZE; xo++) {
            for (int yo = 0; yo < CHUNK_SECTION_SIZE; yo++) {
                for (int zo = 0; zo < CHUNK_SECTION_SIZE; zo++) {
                    if (parentChunk.getBlockState(parentChunk.x + xo, startHeight + yo, parentChunk.z + zo) != null)
                        return false;
                }
            }
        }
        return true;
    }

    public int getStartHeight() {
        return startHeight;
    }

    @NotNull
    public ChunkBase getParentChunk() {
        return parentChunk;
    }
}
