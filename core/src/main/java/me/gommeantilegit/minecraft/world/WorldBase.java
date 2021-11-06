package me.gommeantilegit.minecraft.world;

import com.badlogic.gdx.math.Vector3;
import kotlin.Pair;
import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.block.state.palette.IBlockStatePalette;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.raytrace.IRayTracer;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkSection;
import me.gommeantilegit.minecraft.world.chunk.change.BlockStateSemaphoreBase;
import me.gommeantilegit.minecraft.world.chunk.creator.ChunkCreatorBase;
import me.gommeantilegit.minecraft.world.chunk.creator.OnChunkCreationListener;
import me.gommeantilegit.minecraft.world.chunk.loader.ChunkLoaderBase;
import me.gommeantilegit.minecraft.world.chunk.world.WorldChunkHandlerBase;
import me.gommeantilegit.minecraft.world.entity.EntitySpawner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.lang.Math.*;
import static me.gommeantilegit.minecraft.utils.MathHelper.nearestInt;
import static me.gommeantilegit.minecraft.world.chunk.ChunkBase.CHUNK_SIZE;
import static me.gommeantilegit.minecraft.world.chunk.ChunkSection.CHUNK_SECTION_SIZE;

public abstract class WorldBase implements Tickable, AsyncOperation {

    /**
     * Standard world height
     */
    public static final int STANDARD_WORLD_HEIGHT = 256;

    /**
     * WorldChunkHandlerBase Object storing world chunk lists
     */
    protected WorldChunkHandlerBase worldChunkHandler;

    /**
     * Object capable of loading and unloading chunks
     */
    protected ChunkLoaderBase chunkLoader;

    /**
     * Object capable of creating the chunks to be later loaded and built
     */
    protected ChunkCreatorBase chunkCreator;

    /**
     * The distance that chunks are loaded and rendered in for the player. (the only viewer on the client) (client)
     * The distance that chunks are loaded in for entities that do not have other logic in place to compute this value. (server)
     */
    private int chunkLoadingDistance = 32;

    private final int height;

    /**
     * Minecraft instance
     */
    @NotNull
    public final AbstractMinecraft mc;

    /**
     * The world time in ticks
     */
    public long worldTime = 0;

    /**
     * Default {@link OnServerChunkCreationListener} invoked on chunk creation.
     *
     * @see OnServerChunkCreationListener
     * @see OnServerChunkCreationListener#onChunkCreated(ChunkBase)
     */
    @NotNull
    private List<OnServerChunkCreationListener> onChunkCreationListeners = new ArrayList<>();

    /**
     * The completable future that is completed when the world tick was successfully paused. (null, when requested)
     */
    @Nullable
    private CompletableFuture<Void> pausedFuture;

    @NotNull
    protected final EntitySpawner entitySpawner = new EntitySpawner(this);

    /**
     * The state whether to pause the world tick when the next tick is invoked.
     * When the pause (Thread sleep, so timer performs missing ticks) is performed, the {@link #pausedFuture} is invoked (=> must be not null when this field is true)
     */
    private boolean shouldPauseTick = false;

    /**
     * The block state palette used to store chunks in
     */
    @NotNull
    private final IBlockStatePalette blockStatePalette;

    /**
     * Manages block states
     */
    @NotNull
    protected final BlockStateSemaphoreBase blockStateSemaphore;

    protected WorldBase(@NotNull AbstractMinecraft mc, @NotNull IBlockStatePalette blockStatePalette, @NotNull BlockStateSemaphoreBase blockStateSemaphore) {
        this(mc, STANDARD_WORLD_HEIGHT, blockStatePalette, blockStateSemaphore);
    }

    /**
     * @param mc                  sets {@link #mc}
     * @param height              sets {@link #height}
     * @param blockStatePalette   sets {@link #blockStatePalette}
     * @param blockStateSemaphore sets {@link #blockStateSemaphore}
     */
    protected WorldBase(@NotNull AbstractMinecraft mc, int height, @NotNull IBlockStatePalette blockStatePalette, @NotNull BlockStateSemaphoreBase blockStateSemaphore) {
        this.blockStatePalette = blockStatePalette;
        assert height % CHUNK_SECTION_SIZE == 0;
        this.mc = mc;
        this.height = height;
        this.blockStateSemaphore = blockStateSemaphore;
    }

    @Override
    public void tick(float partialTicks) {
        if (shouldPauseTick) {
            Objects.requireNonNull(this.pausedFuture, "Paused future not initialized when shouldPauseTick is true!").complete(null);
            this.pausedFuture = null;
            while (shouldPauseTick) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    System.err.println("Wold Tick pause thread sleep failed with interrupted exception. (Ignoring... like if nothing happened)");
                    e.printStackTrace();
                }
            }
        }
        List<ChunkBase> loadedChunks = this.worldChunkHandler.getLoadedChunks();
        tickChunks(partialTicks, loadedChunks);
        this.chunkCreator.tick(partialTicks);
        this.chunkLoader.tick(partialTicks);

        this.blockStateSemaphore.tick(partialTicks);
        this.worldTime++;
    }

    protected abstract void tickChunks(float partialTicks, @NotNull Collection<ChunkBase> chunks);

    /**
     * Spawns the given entity into the world
     *
     * @param entity the given entity.
     */
    @ThreadSafe
    public void spawnEntityInWorld(@NotNull Entity entity) {
        entity.setWorld(this);
        this.entitySpawner.spawnEntity(entity);
    }

    /**
     * @param axisAlignedBB the boundingBox
     * @return if an entity is in the given bounding box
     */
    public boolean isEntityFree(@NotNull AxisAlignedBB axisAlignedBB) {
        List<ChunkBase> chunks = getChunksInBoundingBox(axisAlignedBB);
        for (ChunkBase chunk : chunks) {
            if (!chunk.isEntityFree(axisAlignedBB))
                return false;
        }
        return true;
    }

    /**
     * Sets the block at the given position
     *
     * @param x             x position
     * @param y             y position
     * @param z             z position
     * @param newBlockState the new block state
     */
    public void setBlock(int x, int y, int z, @Nullable IBlockState newBlockState) {
        ChunkBase chunk = getChunkForPosition(x, z);
        if (chunk == null)
            throw new IllegalStateException("Couldn't set block state of coordinates [x: " + x + ", y:" + y + ", " + z + "] to blockID: " + newBlockState + ". No ChunkBase containing coordinates.");
        chunk.setBlock(x, y, z, newBlockState);
    }

    @Nullable
    @ThreadSafe
    public IBlockState getBlockState(int x, int y, int z) {
        ChunkBase chunk = this.getChunkForPosition(x, z);
        if (chunk == null) return null;
        else return chunk.getBlockState(x, y, z);
    }

    @Nullable
    @ThreadSafe
    public IBlockState getBlockState(@NotNull BlockPos blockPos) {
        return getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    /**
     * @param posX x position
     * @param posZ z position
     * @return the chunk position for the specified coordinates - meaning where the chunk containing this position starts
     */
    @NotNull
    public Vec2i getChunkOrigin(float posX, float posZ) {
        int xi;
        if (posX >= 0)
            xi = (int) posX;
        else
            xi = (int) floor(posX);

        int zi;
        if (posZ >= 0)
            zi = (int) posZ;
        else
            zi = (int) floor(posZ);

        int chunkX = ((int) floor(xi / (float) CHUNK_SIZE)) * CHUNK_SIZE;
        int chunkZ = ((int) floor(zi / (float) CHUNK_SIZE)) * CHUNK_SIZE;
        return new Vec2i(chunkX, chunkZ);
    }

    /**
     * Performs a ray cast against all blocks in the world. Args : rayStart, direction, range
     */
    @NotNull
    public RayTracer.RayTraceResult rayTraceBlocks(@NotNull Vector3 rayStart, @NotNull Vector3 direction, float range) {
        AxisAlignedBB boundingBox = new AxisAlignedBB(
                rayStart.x, rayStart.y, rayStart.z,
                rayStart.x + direction.x * range,
                rayStart.y + direction.y * range,
                rayStart.z + direction.z * range
        ).reoder();
        IRayTracer.RayTraceResult result = new RayTracer.RayTraceResult(null, null, RayTracer.RayTraceResult.EnumResultType.MISS, null);
        float minDst = -1;
        List<Pair<BlockPos, AxisAlignedBB>> blocks = getBlocksInBoundingBox(boundingBox);
        for (Pair<BlockPos, AxisAlignedBB> pair : blocks) {
            BlockPos pos = pair.getFirst();
            Block block = getBlock(pos);
            if (block == null)
                continue;
            IRayTracer.RayTraceResult blockTrace = block.collisionRayTrace(pos, rayStart, direction, range);
            if (blockTrace.type == IRayTracer.RayTraceResult.EnumResultType.BLOCK) {
                Vector3 hitVec = Objects.requireNonNull(blockTrace.hitVec);
                float hitVecDst = hitVec.cpy().sub(rayStart).len2();

                if (hitVecDst < minDst || minDst == -1) {
                    result = blockTrace;
                    minDst = hitVecDst;
                }
            }
        }

        return result;
    }

    /**
     * @param axisAlignedBB the bounding box.
     * @return chunks managing parts of the region.
     */
    @NotNull
    private List<ChunkBase> getChunksInBoundingBox(@NotNull AxisAlignedBB axisAlignedBB) {
        List<ChunkBase> chunks = new ArrayList<>(4);
        float x0 = axisAlignedBB.x0;
        float z0 = axisAlignedBB.z0;
        float x1 = axisAlignedBB.x1;
        float z1 = axisAlignedBB.z1;
        ChunkBase chunk;
        {
            chunk = getChunkForPosition((int) x0, round(z0));
            chunks.add(chunk);
        }
        {
            if (chunk == null)
                chunk = getChunkForPosition((int) x0, round(z1));
            else
                chunk = getNearChunkFor(chunk, (int) x0, round(z1));
            if (!chunks.contains(chunk))
                chunks.add(chunk);
        }
        {
            if (chunk == null)
                chunk = getChunkForPosition((int) x1, (int) z1);
            else
                chunk = getNearChunkFor(chunk, (int) x1, (int) (z1));
            if (!chunks.contains(chunk))
                chunks.add(chunk);
        }
        {
            if (chunk == null)
                chunk = getChunkForPosition((int) x1, (int) z0);
            else
                chunk = getNearChunkFor(chunk, (int) x1, (int) (z0));
            if (!chunks.contains(chunk))
                chunks.add(chunk);
        }
        return chunks;
    }

    /**
     * Called on entity collision with a bounding box
     *
     * @param entity        the entity which collided
     * @param axisAlignedBB the bounding box it collided with
     */
    public void collision(@NotNull List<AxisAlignedBB> blockABBs, @NotNull Entity entity, @NotNull AxisAlignedBB axisAlignedBB) {
        for (AxisAlignedBB bb : blockABBs) {
            if (bb.x0 == axisAlignedBB.x0 && bb.y0 == axisAlignedBB.y0 &&
                    bb.z0 == axisAlignedBB.z0 && bb.x1 == axisAlignedBB.x1 &&
                    bb.y1 == axisAlignedBB.y1 && bb.z1 == axisAlignedBB.z1) {
                onBlockCollision(entity, axisAlignedBB);
            }
        }
    }

    /**
     * Called on block collision
     *
     * @param entity        the entity which collided
     * @param axisAlignedBB the boundingBox of the block it collided with
     */
    private void onBlockCollision(@NotNull Entity entity, @NotNull AxisAlignedBB axisAlignedBB) {
        int x = (int) axisAlignedBB.x0, y = (int) axisAlignedBB.y0, z = (int) axisAlignedBB.z0;
        ChunkBase forPos = getNearChunkFor(entity.getCurrentChunk(), x, z);
        if (forPos == null)
            return;
        IBlockState state = forPos.getBlockState(x, y, z);
        if (state == null)
            return;
        Block block = state.getBlock();
        block.onEntityCollide(entity, axisAlignedBB);
    }

    @NotNull
    public List<Pair<BlockPos, AxisAlignedBB>> getBlocksInBoundingBox(@NotNull AxisAlignedBB box) {
        List<Pair<BlockPos, AxisAlignedBB>> boxes = new ArrayList<>();
        int x0 = (int) floor(box.x0);
        int x1 = (int) floor(box.x1 + 1.0f);
        int y0 = (int) floor(box.y0);
        int y1 = (int) floor(box.y1 + 1.0f);
        int z0 = (int) floor(box.z0);
        int z1 = (int) floor(box.z1 + 1.0f);
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    AxisAlignedBB blockBB;
                    BlockPos bp = new BlockPos(x, y, z);
                    IBlockState state = getBlockState(bp);
                    if (state != null) {
                        Block block = state.getBlock();
                        if ((blockBB = block.getBoundingBox(this, bp, state)) != null) {
                            boxes.add(new Pair<>(bp, blockBB));
                        }
                    }
                }
            }
        }
        return boxes;
    }

    /**
     * @param box the bounding box
     * @return all bounding boxes of the blockStates in the given bounding box.
     */
    @NotNull
    public List<AxisAlignedBB> getBoundingBoxes(@NotNull AxisAlignedBB box) {
        List<AxisAlignedBB> boxes = new ArrayList<>();
        int x0 = (int) nearestInt(box.x0);
        int x1 = (int) nearestInt(box.x1 + 1.0f);
        int y0 = (int) nearestInt(box.y0);
        int y1 = (int) nearestInt(box.y1 + 1.0f);
        int z0 = (int) nearestInt(box.z0);
        int z1 = (int) nearestInt(box.z1 + 1.0f);
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    AxisAlignedBB axisAlignedBB2;
                    BlockPos bp = new BlockPos(x, y, z);

                    IBlockState state = getBlockState(bp);
                    if (state != null) {
                        Block block = state.getBlock();
                        if ((axisAlignedBB2 = block.getBoundingBox(this, bp, state)) != null) {
                            boxes.add(axisAlignedBB2);
                        }
                    }
                }
            }
        }
        return boxes;
    }



    /**
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return the block instance at the specified coordinates or null for air
     */
    @Nullable
    @ThreadSafe
    public Block getBlock(int x, int y, int z) {
        IBlockState blockState = getBlockState(x, y, z);
        if (blockState == null)
            return null;
        return blockState.getBlock();
    }

    @Nullable
    @ThreadSafe
    public Block getBlock(@NotNull BlockPos blockPos) {
        IBlockState blockState = getBlockState(blockPos);
        if (blockState == null)
            return null;
        return blockState.getBlock();
    }

    /**
     * Assumes that the result chunk is near to the current chunk. This algorithm is approximately O(Distance) worst case
     *
     * @param chunk  the chunk that the chunk requested is near to
     * @param entity the given entity that the chunk that is searched for contains
     * @return the chunk that contains the entity
     */
    @Nullable
    @ThreadSafe
    public ChunkBase getNearChunkFor(@NotNull ChunkBase chunk, @NotNull Entity entity) {
        Vec2i chunkPos = getChunkOrigin(entity.posX, entity.posZ);
        return traverseChunks(chunk, chunkPos);
    }

    /**
     * Assumes that the result chunk is near to the current chunk. This algorithm is approximately O(Distance) worst case
     *
     * @param chunk the chunk that the chunk requested is near to
     * @param x     the x component of the position that the chunk that is searched for contains
     * @param z     the z component of the position that the chunk that is searched for contains
     * @return the chunk that contains the block position
     */
    @Nullable
    @ThreadSafe
    public ChunkBase getNearChunkFor(@NotNull ChunkBase chunk, int x, int z) {
        Vec2i chunkPos = getChunkOrigin(x, z);
        return traverseChunks(chunk, chunkPos);
    }

    /**
     * Should be used when there is no reference chunk that the result chunk is near to. If there is any chunk available to supply to the search at all, use {@link #getNearChunkFor(ChunkBase, Entity)} it will be faster.
     *
     * @param entity the given entity
     * @return the chunk that contains the entity
     */
    @Nullable
    @ThreadSafe
    public ChunkBase getAbsoluteChunkFor(@NotNull Entity entity) {
        Vec2i chunkPos = getChunkOrigin(entity.posX, entity.posZ);
        return getChunkAtOrigin(chunkPos.getX(), chunkPos.getY());
    }

    /**
     * Traverses the minecraft world like 2D LinkedList to the chunk position. This algorithm is approximately O(Distance) worst case
     *
     * @param from     the starting chunk to start the search
     * @param chunkPos the chunk origin of the chunk that is searched for
     * @return the chunk, or null if not found.
     */
    @Nullable
    @ThreadSafe // if the chunk link does not exist yet, it just doesn't
    protected ChunkBase traverseChunks(@NotNull ChunkBase from, @NotNull Vec2i chunkPos) {
        Vec2i fromPos = from.getChunkOrigin();
        int xDif = chunkPos.getX() - fromPos.getX();
        int zDif = chunkPos.getY() - fromPos.getY();
        int xDir = Integer.compare(xDif, 0);

        ChunkBase chunk = from;

        int zDir = Integer.compare(zDif, 0);
        // traverse z
        if (zDir != 0) {
            int zLen = abs(zDif);
            int neighborIndex = zDir == 1 ? 0 : 1;
            for (int z = 0; z < zLen && chunk != null; z += CHUNK_SIZE) {
                chunk = chunk.getNeighbor(neighborIndex);
            }
        }
        // traverse x
        if (xDir != 0 && chunk != null) {
            int xLen = abs(xDif);
            int neighborIndex = xDir == 1 ? 2 : 3;
            for (int x = 0; x < xLen && chunk != null; x += CHUNK_SIZE) {
                chunk = chunk.getNeighbor(neighborIndex);
            }
        }
        return chunk;
    }


    /**
     * @param x x component of the position vector
     * @param z y component of the position vector
     * @return the chunk that handles the square coordinate range of CHUNK_SIZE by CHUNK_SIZE that contains the given coordinate vector
     */
    @Nullable
    @ThreadSafe
    public ChunkBase getChunkForPosition(float x, float z) {
        Vec2i chunkPos = getChunkOrigin(x, z);
        return getChunkAtOrigin(chunkPos.getX(), chunkPos.getY());
    }

    /**
     * @param originX x position
     * @param originZ z position
     * @return the parent chunk of the given position.
     */
    @Nullable
    @ThreadSafe
    public ChunkBase getChunkAtOrigin(int originX, int originZ) {
        return this.worldChunkHandler.getChunkAt(originX, originZ);
    }

    @NotNull
    public EntitySpawner getEntitySpawner() {
        return entitySpawner;
    }

    @NotNull
    public WorldChunkHandlerBase getWorldChunkHandler() {
        return worldChunkHandler;
    }

    /**
     * Stops all async work the world is doing
     */
    @Override
    public void stopAsyncWork() {
    }

    @NotNull
    public ChunkLoaderBase getChunkLoader() {
        return Objects.requireNonNull(chunkLoader, "ChunkLoader not yet initialized!");
    }

    @NotNull
    public ChunkCreatorBase getChunkCreator() {
        return chunkCreator;
    }

    public int getChunkLoadingDistance() {
        return chunkLoadingDistance;
    }

    public void setChunkLoadingDistance(int chunkLoadingDistance) {
        this.chunkLoadingDistance = chunkLoadingDistance;
        this.chunkLoader.trackChunkLoadingDistanceChange(chunkLoadingDistance);
    }

    @NotNull
    public Future<Void> pauseTick() {
        this.shouldPauseTick = true;
        if (this.pausedFuture == null) {
            this.pausedFuture = new CompletableFuture<>();
        }
        return this.pausedFuture;
    }

    public void resumeTick() {
        this.shouldPauseTick = false;
    }

    /**
     * World Height (Max build height).
     * Must be a multiple of {@link ChunkSection#CHUNK_SECTION_SIZE} and thus {@link ChunkBase#CHUNK_SIZE}
     */
    public int getHeight() {
        return height;
    }

    @NotNull
    public IBlockStatePalette getBlockStatePalette() {
        return blockStatePalette;
    }

    @NotNull
    public BlockStateSemaphoreBase getBlockStateSemaphore() {
        return blockStateSemaphore;
    }

    public interface OnServerChunkCreationListener extends OnChunkCreationListener {
    }

    @NotNull
    public WorldBase addOnChunkCreationListener(@Nullable OnServerChunkCreationListener onChunkCreationListener) {
        this.onChunkCreationListeners.add(onChunkCreationListener);
        return this;
    }

    @NotNull
    public List<OnServerChunkCreationListener> getOnChunkCreationListeners() {
        return onChunkCreationListeners;
    }
}
