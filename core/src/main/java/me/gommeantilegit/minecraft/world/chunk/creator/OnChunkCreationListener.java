package me.gommeantilegit.minecraft.world.chunk.creator;

import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;

/**
 * Object representing a listener listening to the creation of new chunks
 */
public interface OnChunkCreationListener<C extends ChunkBase> {
    /**
     * Called when a chunks is created and added to the world
     *
     * @param chunk the chunk, that is added
     */
    void onChunkCreated(@NotNull C chunk);
}
