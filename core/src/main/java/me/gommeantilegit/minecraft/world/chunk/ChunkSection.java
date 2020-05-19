package me.gommeantilegit.minecraft.world.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.block.state.storage.BlockStateStorage;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static me.gommeantilegit.minecraft.world.chunk.ChunkBase.CHUNK_SIZE;

public class ChunkSection {

    /**
     * X, Y, Z size of a Chunk Section
     */
    public static final int CHUNK_SECTION_SIZE = CHUNK_SIZE;

    /**
     * The parent chunk
     */
    @NotNull
    private final ChunkBase parentChunk;

    /**
     * The y coordinate where the chunk section starts. Must be a multiple of {@link #CHUNK_SECTION_SIZE}
     */
    private final int startHeight;

    /**
     * The bounding box of the chunk section
     */
    @NotNull
    private final BoundingBox boundingBox;

    /**
     * Stores the block states of the sections
     */
    @NotNull
    protected final BlockStateStorage blockStateStorage;

//    /**
//     * Lock to synchronize block storage access
//     */
//    @NotNull
//    private final Lock blockStorageLock = new ReentrantLock();

    public ChunkSection(@NotNull ChunkBase parentChunk, int startHeight) {
        this.parentChunk = parentChunk;
        this.startHeight = startHeight;
        this.blockStateStorage = new BlockStateStorage(CHUNK_SIZE, CHUNK_SECTION_SIZE, CHUNK_SIZE, parentChunk.getBlockStatePalette());
        this.boundingBox = new BoundingBox(new Vector3(parentChunk.x, startHeight, parentChunk.z), new Vector3(parentChunk.x + CHUNK_SECTION_SIZE, startHeight + CHUNK_SECTION_SIZE, parentChunk.z + CHUNK_SECTION_SIZE));
    }

    /**
     * @return true if the chunk section is fully empty meaning that every block is air in the given section of the {@link #parentChunk}
     */
    @ThreadSafe
    public boolean isEmpty() {
        for (int xo = 0; xo < CHUNK_SIZE; xo++) {
            for (int yo = 0; yo < CHUNK_SECTION_SIZE; yo++) {
                for (int zo = 0; zo < CHUNK_SIZE; zo++) {
                    if (getRelativeBlockState(xo, yo, zo) != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns the block-state at the specified section inner relative coordinates or null, if the state is air
     */
    @Nullable
    @ThreadSafe
    public IBlockState getRelativeBlockState(int x, int y, int z) {
        check(x, y, z);
        IBlockState blockState;
        synchronized (this.blockStateStorage) {
            blockState = this.blockStateStorage.getBlockState(x, y, z);
        }
        return blockState;
    }

    /**
     * Sets the block-state at the specified section inner relative coordinates or null, if the state is air
     */
    @ThreadSafe
    public void setBlockState(int x, int y, int z, @Nullable IBlockState blockState) {
        check(x, y, z);
        synchronized (this.blockStateStorage) {
            this.blockStateStorage.set(x, y, z, blockState);
        }
    }

    public int getStartHeight() {
        return startHeight;
    }

    @NotNull
    public ChunkBase getParentChunk() {
        return parentChunk;
    }

    @NotNull
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    @NotNull
    public BlockStateStorage getBlockStorageCopy() {
        return this.blockStateStorage.copy();
    }

    private void check(int x, int y, int z) {
        if (y < 0 || y > CHUNK_SECTION_SIZE)
            throw new IllegalArgumentException("Y-Coordinate " + y + " not in relative chunk section bounds.");
        if (x < 0 || x > CHUNK_SIZE)
            throw new IllegalArgumentException("X-Coordinate " + y + " not in relative chunk section bounds.");
        if (z < 0 || z > CHUNK_SIZE)
            throw new IllegalArgumentException("Z-Coordinate " + z + " not in relative chunk section bounds.");
    }

    /**
     * Sets all blocks of the section to air
     */
    @ThreadSafe
    public void clearBlocks() {
        synchronized (this.blockStateStorage) {
            this.blockStateStorage.clear();
        }
    }

    /**
     * Applies the chunk data to the chunk section
     *
     * @param chunkData the chunk block state palette data
     */
    @ThreadSafe
    public void apply(@NotNull byte[] chunkData) {
        synchronized (this.blockStateStorage) {
            this.blockStateStorage.apply(chunkData);
        }
    }

    /**
     * @return a copy of the palette data
     */
    @NotNull
    @ThreadSafe
    public byte[] getPaletteData() {
        @NotNull byte[] data;
        synchronized (this.blockStateStorage) {
            data = this.blockStateStorage.getPaletteData();
        }
        return data;
    }

    /**
     * @return all neighbors index-able from index to neighbor position --> no section at position present --> neighbors.get(index) = null
     */
    @NotNull
    public List<ChunkSection> getNeighboringSections() {
        List<ChunkSection> neighborSections = new ArrayList<>(6);
        WorldBase world = parentChunk.getWorld();
        int chunkX = parentChunk.getX(), chunkZ = parentChunk.getZ();
        ChunkBase chunk = world.getChunkAtOrigin(chunkX - CHUNK_SIZE, chunkZ);
        if (chunk != null) {
            neighborSections.add(chunk.getChunkSection(startHeight));
        } else {
            neighborSections.add(null);
        }
        chunk = world.getChunkAtOrigin(chunkX + CHUNK_SIZE, chunkZ);
        if (chunk != null) {
            neighborSections.add(chunk.getChunkSection(startHeight));
        } else {
            neighborSections.add(null);
        }
        chunk = world.getChunkAtOrigin(chunkX, chunkZ - CHUNK_SIZE);
        if (chunk != null) {
            neighborSections.add(chunk.getChunkSection(startHeight));
        } else {
            neighborSections.add(null);
        }
        chunk = world.getChunkAtOrigin(chunkX, chunkZ + CHUNK_SIZE);
        if (chunk != null) {
            neighborSections.add(chunk.getChunkSection(startHeight));
        } else {
            neighborSections.add(null);
        }
        if (startHeight >= CHUNK_SECTION_SIZE) {
            neighborSections.add(parentChunk.getChunkSection(startHeight - CHUNK_SECTION_SIZE));
        } else {
            neighborSections.add(null);
        }
        if (startHeight < parentChunk.getHeight() - CHUNK_SECTION_SIZE) {
            neighborSections.add(parentChunk.getChunkSection(startHeight + CHUNK_SECTION_SIZE));
        } else {
            neighborSections.add(null);
        }
        return neighborSections;
    }
}
