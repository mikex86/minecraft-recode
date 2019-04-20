package me.gommeantilegit.minecraft.world.chunk.world;

import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.utils.collections.LongHashMap;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class WorldChunkHandlerBase {

    /**
     * Stores all currently loaded chunks
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final ArrayList<ChunkBase> loadedChunks;

    /**
     * Stores all currently unloaded chunks
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final ArrayList<ChunkBase> unloadedChunks;

    /**
     * List storing all chunks of the world.
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final ArrayList<ChunkBase> chunks;

    /**
     * A map containing all registered chunk origins and their parent chunks at that origin.
     * chunkMap.get(originHash) may return null, if the chunk is currently scheduled to be created
     */
    @NotNull
    private final LongHashMap<ChunkBase> chunkMap = new LongHashMap<>();

    /**
     * Hash set of all chunk origins
     */
    @NotNull
    private final LongHashSet chunkOrigins = new LongHashSet();

    public WorldChunkHandlerBase() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public WorldChunkHandlerBase(@NotNull ArrayList<ChunkBase> chunks, @NotNull ArrayList<ChunkBase> unloadedChunks, @NotNull ArrayList<ChunkBase> loadedChunks) {
        this.chunks = chunks;
        this.unloadedChunks = unloadedChunks;
        this.loadedChunks = loadedChunks;
    }

    /**
     * @param vec the x, z vector
     * @return true if a chunk that handles those coordinates exists
     */
    public synchronized boolean chunkExistsAtOrigin(@NotNull Vec2i vec) {
        return this.chunkOrigins.contains(vec.hash64());
    }

    /**
     * Adds the given chunk origin to {@link #chunkOrigins}. This means {@link #getChunkAt(int, int)} for the specified chunk origin will
     * return true, but {@link #getChunkAt(int, int)} for the specified origin might still return null as creation is currently done asynchronously
     *
     * @param chunkOrigin the origin of the chunk whose creation has just been scheduled.
     */
    public synchronized void addChunkOrigin(@NotNull Vec2i chunkOrigin) {
        this.chunkOrigins.add(chunkOrigin.hash64());
    }

    /**
     * @param originX the x component of the origin
     * @param originZ the z component of the origin
     * @return the chunk instance for the specified position
     */
    @Nullable
    public synchronized ChunkBase getChunkAt(int originX, int originZ) {
        return this.chunkMap.get(Vec2i.hash64(originX, originZ));
    }

    /**
     * Adds the specified chunk to the chunk list and registers it in the chunk origin hash map
     *
     * @param chunk the new chunk
     */
    public synchronized void addChunk(@NotNull ChunkBase chunk) {
        this.chunks.add(chunk);
        this.chunkMap.put(Vec2i.hash64(chunk.getX(), chunk.getZ()), chunk);
    }

    @NotNull
    public synchronized ArrayList<ChunkBase> getChunks() {
        return chunks;
    }

    @NotNull
    public synchronized ArrayList<ChunkBase> getUnloadedChunks() {
        return unloadedChunks;
    }

    @NotNull
    public synchronized ArrayList<ChunkBase> getLoadedChunks() {
        return loadedChunks;
    }

    public synchronized void addLoadedChunk(@NotNull ChunkBase chunk) {
        if (!loadedChunks.contains(chunk))
            this.loadedChunks.add(chunk);
    }

    public synchronized void addUnloadedChunk(ChunkBase chunk) {
        if (!unloadedChunks.contains(chunk))
            this.unloadedChunks.add(chunk);
    }

    public synchronized void removeLoadedChunk(ChunkBase chunk) {
        this.loadedChunks.remove(chunk);
    }

    public synchronized void removeUnloadedChunk(@NotNull ChunkBase chunk) {
        this.unloadedChunks.remove(chunk);
    }
}
