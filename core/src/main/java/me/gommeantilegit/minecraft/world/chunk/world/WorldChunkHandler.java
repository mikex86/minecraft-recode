package me.gommeantilegit.minecraft.world.chunk.world;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.util.collections.ThreadBoundList;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;

public class WorldChunkHandler {

    /**
     * Stores all currently loaded chunks
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final ThreadBoundList<Chunk> loadedChunks = new ThreadBoundList<>(Minecraft.mc.minecraftThread);

    /**
     * Stores all currently unloaded chunks
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final ThreadBoundList<Chunk> unloadedChunks = new ThreadBoundList<>(Minecraft.mc.minecraftThread);

    /**
     * List storing all chunks of the world.
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final ThreadBoundList<Chunk> chunks = new ThreadBoundList<>(Minecraft.mc.minecraftThread);

    /**
     * Stores the origins of all created chunks.
     */
    @NotNull
    private final Queue<Vector2> chunkOrigins = new LinkedList<>();

    /**
     * Manager Object for handling chunk iteration for world rendering
     */
    @NotNull
    private final ChunkRenderManager chunkRenderManager = new ChunkRenderManager(this);

    /**
     * @param vec the x, z vector
     * @return true if a chunk that handles those coordinates exists
     */
    public boolean chunkExistsAt(Vector2 vec) {
        return this.chunkOrigins.contains(vec);
    }

    /**
     * Adds the given chunk origin to {@link #chunkOrigins}
     * @param chunkOrigin the origin of the chunk whose creation has just been scheduled.
     */
    public void addChunkOrigin(@NotNull Vector2 chunkOrigin){
        this.chunkOrigins.add(chunkOrigin);
    }

    @NotNull
    public ThreadBoundList<Chunk> getChunks() {
        return chunks;
    }

    @NotNull
    public ThreadBoundList<Chunk> getUnloadedChunks() {
        return unloadedChunks;
    }

    @NotNull
    public ThreadBoundList<Chunk> getLoadedChunks() {
        return loadedChunks;
    }

    @NotNull
    public Queue<Vector2> getChunkOrigins() {
        return chunkOrigins;
    }

    @NotNull
    public ChunkRenderManager getChunkRenderManager() {
        return this.chunkRenderManager;
    }

    public void addLoadedChunk(@NotNull Chunk chunk) {
        this.loadedChunks.add(chunk);
    }

    public void addUnloadedChunk(Chunk chunk) {
        this.unloadedChunks.add(chunk);
    }

    public void removeLoadedChunk(Chunk chunk) {
        this.loadedChunks.remove(chunk);
    }

    public void removeUnloadedChunk(@NotNull Chunk chunk) {
        this.unloadedChunks.remove(chunk);
    }
}
