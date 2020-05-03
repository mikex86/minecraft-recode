package me.gommeantilegit.minecraft.world.chunk.builder;

public enum RebuildPriority {

    /**
     * Chunk is rebuilt in the normal interval by the {@link ChunkMeshRebuilder} object
     */
    NORMAL,

    /**
     * Chunk is rebuilt next frame when the chunk is rendered
     */
    HIGH
}
