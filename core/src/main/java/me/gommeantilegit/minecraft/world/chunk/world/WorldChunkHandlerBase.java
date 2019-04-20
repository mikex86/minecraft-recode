package me.gommeantilegit.minecraft.world.chunk.world;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.block.BlockBase;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.block.state.BlockStateBase;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.utils.collections.LongHashMap;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class WorldChunkHandlerBase<CB extends ChunkBase, MC extends AbstractMinecraft<BB, MC, BLOCKS, BS>, BB extends BlockBase<MC, BB, BS, BLOCKS>, BLOCKS extends Blocks<BB, MC>, BS extends BlockStateBase<BB>> {

    /**
     * Stores all currently loaded chunks
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final ArrayList<CB> loadedChunks;

    /**
     * Stores all currently unloaded chunks
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final ArrayList<CB> unloadedChunks;

    /**
     * List storing all chunks of the world.
     * Only accessible from Minecraft Thread!
     */
    @NotNull
    private final ArrayList<CB> chunks;

    /**
     * A map containing all registered chunk origins and their parent chunks at that origin.
     * chunkMap.get(originHash) may return null, if the chunk is currently scheduled to be created
     */
    @NotNull
    private final LongHashMap<CB> chunkMap = new LongHashMap<>();

    /**
     * Hash set of all chunk origins
     */
    @NotNull
    private final LongHashSet chunkOrigins = new LongHashSet();

    public WorldChunkHandlerBase() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public WorldChunkHandlerBase(@NotNull ArrayList<CB> chunks, @NotNull ArrayList<CB> unloadedChunks, @NotNull ArrayList<CB> loadedChunks) {
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
    public synchronized CB getChunkAt(int originX, int originZ) {
        return this.chunkMap.get(Vec2i.hash64(originX, originZ));
    }

    /**
     * Adds the specified chunk to the chunk list and registers it in the chunk origin hash map
     *
     * @param chunk the new chunk
     */
    public synchronized void addChunk(@NotNull CB chunk) {
        this.chunks.add(chunk);
        this.chunkMap.put(Vec2i.hash64(chunk.getX(), chunk.getZ()), chunk);
    }

    @NotNull
    public synchronized ArrayList<CB> getChunks() {
        return chunks;
    }

    @NotNull
    public synchronized ArrayList<CB> getUnloadedChunks() {
        return unloadedChunks;
    }

    @NotNull
    public synchronized ArrayList<CB> getLoadedChunks() {
        return loadedChunks;
    }

    public synchronized void addLoadedChunk(@NotNull CB chunk) {
        if (!loadedChunks.contains(chunk))
            this.loadedChunks.add(chunk);
    }

    public synchronized void addUnloadedChunk(CB chunk) {
        if (!unloadedChunks.contains(chunk))
            this.unloadedChunks.add(chunk);
    }

    public synchronized void removeLoadedChunk(CB chunk) {
        this.loadedChunks.remove(chunk);
    }

    public synchronized void removeUnloadedChunk(@NotNull CB chunk) {
        this.unloadedChunks.remove(chunk);
    }
}
