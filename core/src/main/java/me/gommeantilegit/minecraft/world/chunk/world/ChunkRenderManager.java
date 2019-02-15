package me.gommeantilegit.minecraft.world.chunk.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import me.gommeantilegit.minecraft.world.chunk.Chunk;

/**
 * Object for fluent Chunk Iteration for world rendering
 */
public class ChunkRenderManager {

    @NotNull
    private final WorldChunkHandler worldChunkHandler;

    @NotNull
    private final Queue<Chunk> chunks = new LinkedList<>();

    public ChunkRenderManager(@NotNull WorldChunkHandler worldChunkHandler) {
        this.worldChunkHandler = worldChunkHandler;
    }


    @NotNull
    public WorldChunkHandler getWorldChunkHandler() {
        return worldChunkHandler;
    }

    /**
     * Index for iteration
     */
    private int iterationIndex = 0;

    /**
     * Amount of chunks to be iterated over
     */
    private int chunkCount = 0;

    public void newFrame() {
        chunks.clear();
        chunks.addAll(this.worldChunkHandler.getLoadedChunks());
        chunkCount = chunks.size();
        iterationIndex = 0;
    }

    public boolean hasNext() {
        return iterationIndex < chunkCount;
    }

    @Nullable
    public Chunk nextChunk() {
        if (!chunks.isEmpty()) {
            iterationIndex++;
            try {
                return chunks.remove();
            } catch (NoSuchElementException ignored) {
                return null;
            }
        } else return null;
    }
}
