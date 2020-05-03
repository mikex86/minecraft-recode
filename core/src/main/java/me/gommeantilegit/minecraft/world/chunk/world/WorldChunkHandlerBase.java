package me.gommeantilegit.minecraft.world.chunk.world;

import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class WorldChunkHandlerBase {

    /**
     * Stores all currently loaded chunks
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final List<ChunkBase> loadedChunks;

    /**
     * Stores all currently unloaded chunks
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final List<ChunkBase> unloadedChunks;

    /**
     * List storing all chunks of the world.
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final List<ChunkBase> chunks;

    /**
     * A map containing all registered chunk origins and their parent chunks at that origin.
     * chunkMap.get(originHash) may return null, if the chunk is currently scheduled to be created
     */
    @NotNull
    private final Map<Long, ChunkBase> chunkMap = new ConcurrentHashMap<>();

//    /**
//     * Hash set of all chunk origins
//     */
//    @NotNull
//    private final Set<Long> chunkOrigins = new ConcurrentSet<>();

    /**
     * List of uninitialized chunks that are neither loaded or unloaded
     */
    @NotNull
    private final List<ChunkBase> uninitializedChunks = new CopyOnWriteArrayList<>();

    public WorldChunkHandlerBase() {
        this.chunks = new CopyOnWriteArrayList<>();
        this.unloadedChunks = new CopyOnWriteArrayList<>();
        this.loadedChunks = new CopyOnWriteArrayList<>();
    }

    /**
     * @param vec the x, z vector
     * @return true if a chunk that handles those coordinates exists
     */
    @ThreadSafe
    public boolean chunkExistsAtOrigin(@NotNull Vec2i vec) {
        return getChunkAt(vec.getX(), vec.getY()) != null;
    }

    /**
     * @param originX the x component of the origin
     * @param originZ the z component of the origin
     * @return the chunk instance for the specified position
     */
    @Nullable
    @ThreadSafe
    public ChunkBase getChunkAt(int originX, int originZ) {
        return this.chunkMap.get(Vec2i.hash64(originX, originZ));
    }

    /**
     * Adds the specified chunk to the chunk list and registers it in the chunk origin hash map
     *
     * @param chunk the new chunk
     */
    @ThreadSafe
    public synchronized void addChunk(@NotNull ChunkBase chunk) {
        if (getChunkAt(chunk.getX(), chunk.getZ()) != null)
            return;

        long hash = Vec2i.hash64(chunk.getX(), chunk.getZ());
        this.chunks.add(chunk);
        this.chunkMap.put(hash, chunk);
        this.uninitializedChunks.add(chunk);
    }

    @NotNull
    @ThreadSafe
    public List<ChunkBase> collectChunks() {
        return chunks;
    }

    @NotNull
    @ThreadSafe
    public List<ChunkBase> getLoadedChunks() {
        return loadedChunks;
    }

    @ThreadSafe
    public synchronized void loadChunk(@NotNull ChunkBase chunk) {
        this.uninitializedChunks.remove(chunk);

        if (!this.loadedChunks.contains(chunk))
            this.loadedChunks.add(chunk);

        this.unloadedChunks.remove(chunk);
    }

    @ThreadSafe
    public synchronized void unloadChunk(@NotNull ChunkBase chunk) {
        this.uninitializedChunks.remove(chunk);

        if (!this.unloadedChunks.contains(chunk))
            this.unloadedChunks.add(chunk);
        this.loadedChunks.remove(chunk);
    }

    @NotNull
    public Stream<Reference<ChunkBase>> getLoadableChunks() {
        return Stream.concat(this.unloadedChunks.parallelStream().map(WeakReference::new), this.uninitializedChunks.parallelStream().map(WeakReference::new));
    }
}
