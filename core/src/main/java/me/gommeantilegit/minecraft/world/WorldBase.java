package me.gommeantilegit.minecraft.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.timer.Timer;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkSection;
import me.gommeantilegit.minecraft.world.chunk.creator.ChunkCreatorBase;
import me.gommeantilegit.minecraft.world.chunk.creator.OnChunkCreationListener;
import me.gommeantilegit.minecraft.world.chunk.loader.ChunkLoaderBase;
import me.gommeantilegit.minecraft.world.chunk.world.WorldChunkHandlerBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.*;
import static me.gommeantilegit.minecraft.world.chunk.ChunkBase.CHUNK_SIZE;
import static me.gommeantilegit.minecraft.world.chunk.ChunkSection.CHUNK_SECTION_SIZE;

public abstract class WorldBase implements Tickable, Runnable, AsyncOperation {

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
     * State if the world is fully operational
     */
    public boolean isLoaded = false;

    /**
     * The world's thread for async operations.
     */
    public Thread worldThread;

    /**
     * Object capable of creating the chunks to be later loaded and built
     */
    protected ChunkCreatorBase chunkCreator;

    /**
     * Player's distance to chunk in order to be loaded.
     * Also used as render distance by client
     */
    private int chunkLoadingDistance = 64;

    /**
     * World Height (Max build height).
     * Must be a multiple of {@link ChunkSection#CHUNK_SECTION_SIZE} and thus {@link ChunkBase#CHUNK_SIZE}
     */
    public final int height;

    /**
     * World Async Thread for dirty work
     */
    @NotNull
    public final Timer worldThreadTimer = new Timer(20.0f);

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

    protected WorldBase(@NotNull AbstractMinecraft mc) {
        this(mc, 256);
    }

    /**
     * @param mc     sets {@link #mc}
     * @param height sets {@link #height}
     */
    protected WorldBase(@NotNull AbstractMinecraft mc, int height) {
        assert height % CHUNK_SECTION_SIZE == 0;
        this.mc = mc;
        this.height = height;
    }

    @Override
    public void run() {
        try {
            while (mc.isRunning()) {
                worldThreadTimer.advanceTime();
                for (int i = 0; i < worldThreadTimer.ticks; i++) {
                    worldThreadTimer.tick(worldThreadTimer.partialTicks);
                    try {
                        onAsyncThread();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Thread.sleep(10);
            }
            System.out.println("World async thread terminated.");
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void tick(float partialTicks) {
        if (!isLoaded)
            return;
        ArrayList<ChunkBase> loadedChunks = this.worldChunkHandler.getLoadedChunks();
        for (int i = 0; i < loadedChunks.size(); i++) {
            ChunkBase chunk = loadedChunks.get(i);
            chunk.tick(partialTicks);
        }
        worldTime++;
    }

    /**
     * Spawns the given entity into the world
     *
     * @param entity the given entity.
     */
    public void spawnEntityInWorld(Entity entity) {
        entity.setWorld(this);
    }

    /**
     * @param axisAlignedBB the boundingBox
     * @return if an entity is in the given bounding box
     */
    public boolean isFree(AxisAlignedBB axisAlignedBB) {
        List<ChunkBase> chunks = getChunksInBoundingBox(axisAlignedBB);
        for (ChunkBase chunk : chunks) {
            if (!chunk.isFree(axisAlignedBB))
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
    public void setBlock(int x, int y, int z, @Nullable BlockState newBlockState) {
        ChunkBase chunk = getChunkForPosition(x, z);
        if (chunk == null)
            throw new IllegalStateException("Couldn't set block state of coordinates [x: " + x + ", y:" + y + ", " + z + "] to blockID: " + newBlockState + ". No ChunkBase containing coordinates.");
        chunk.setBlock(x, y, z, newBlockState);
    }

    @Nullable
    public BlockState getBlockState(int x, int y, int z) {
        ChunkBase chunk = this.getChunkForPosition(x, z);
        if (chunk == null) return null;
        else return chunk.getBlockState(x, y, z);
    }

    @Nullable
    public BlockState getBlockState(@NotNull BlockPos blockPos) {
        return getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    /**
     * @param posX x position
     * @param posZ z position
     * @return the chunk position for the specified coordinates - meaning where the chunk containing this position starts
     */
    @NotNull
    public Vec2i getChunkOrigin(float posX, float posZ) {
        int xi = (int) ceil(posX), zi = (int) ceil(posZ);
        int chunkX = ((int) floor(xi / (float) CHUNK_SIZE)) * CHUNK_SIZE;
        int chunkZ = ((int) floor(zi / (float) CHUNK_SIZE)) * CHUNK_SIZE;
        return new Vec2i(chunkX, chunkZ);
    }

    /**
     * Performs a ray cast against all blocks in the world. Args : rayStart, direction, range
     */
    @NotNull
    public RayTracer.RayTraceResult rayTraceBlocks(Vector3 rayStart, Vector3 direction, float range) {
        Ray ray = new Ray(rayStart, direction);
        {
            for (float checkingRange = 0; checkingRange <= range; checkingRange += 0.5f) {
                Vector3 endPoint = ray.getEndPoint(new Vector3(), checkingRange);
                BlockPos blockPos = new BlockPos(endPoint);
                Collection<BlockPos> neighbors = blockPos.getNeighbors();
                boolean performDistanceCheck = false;

                @Nullable
                RayTracer.RayTraceResult acceptedResult = null;

                float smallestDst2 = -1.0f;
                for (BlockPos pos : neighbors) {
                    Block block = this.getBlock(pos);
                    if (block != null) {
                        RayTracer.RayTraceResult result = block.collisionRayTrace(pos, rayStart, direction, range);
                        if (result.type != RayTracer.RayTraceResult.EnumResultType.MISS) {
                            performDistanceCheck = true;
                            assert result.hitVec != null;
                            float currentDst2 = rayStart.dst2(result.hitVec);
                            if (smallestDst2 == -1.0f || currentDst2 < smallestDst2) {
                                acceptedResult = result;
                                smallestDst2 = currentDst2;
                            }
                        }
                    }
                }
                if (performDistanceCheck)
                    return acceptedResult;
            }
        }
        return new RayTracer.RayTraceResult(null, null, RayTracer.RayTraceResult.EnumResultType.MISS, null);
    }

    /**
     * @param x x position
     * @param y y position
     * @param z z position
     * @return the id of the block at the given position
     */
    public int getBlockID(int x, int y, int z) {
        IBlockState blockState = getBlockState(x, y, z);
        if (blockState == null)
            return 0;
        return blockState.getBlock().getId();
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
        {
            ChunkBase chunk = getChunkForPosition((int) x0, round(z0));
            assert chunk != null;
            chunks.add(chunk);
        }
        {
            ChunkBase chunk = getChunkForPosition((int) x0, round(z1));
            assert chunk != null;
            if (!chunks.contains(chunk))
                chunks.add(chunk);
        }
        {
            ChunkBase chunk = getChunkForPosition((int) x1, (int) z1);
            assert chunk != null;
            if (!chunks.contains(chunk))
                chunks.add(chunk);
        }
        {
            ChunkBase chunk = getChunkForPosition((int) x1, (int) z0);
            assert chunk != null;
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
    public void collision(ArrayList<AxisAlignedBB> blockABBs, Entity entity, AxisAlignedBB axisAlignedBB) {
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
    private void onBlockCollision(Entity entity, AxisAlignedBB axisAlignedBB) {
        int x = (int) axisAlignedBB.x0 + 1, y = (int) axisAlignedBB.y0 + 1, z = (int) axisAlignedBB.z0 + 1;
        Block block = mc.blocks.getBlockByID(getBlockID(x, y, z));
        assert block != null;
        block.onEntityCollide(entity, axisAlignedBB);
    }

    /**
     * @param box the bounding box
     * @return all bounding boxes of the blockStates in the given bounding box.
     */
    public ArrayList<AxisAlignedBB> getBoundingBoxes(AxisAlignedBB box) {
        ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
        int x0 = (int) Math.floor(box.x0);
        int x1 = (int) Math.floor(box.x1 + 1.0f);
        int y0 = (int) Math.floor(box.y0);
        int y1 = (int) Math.floor(box.y1 + 1.0f);
        int z0 = (int) Math.floor(box.z0);
        int z1 = (int) Math.floor(box.z1 + 1.0f);
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
     * @return the block instance at the specified coordinates.
     */
    @Nullable
    private Block getBlock(int x, int y, int z) {
        return mc.blocks.getBlockByID(getBlockID(x, y, z));
    }

    @Nullable
    public Block getBlock(@NotNull BlockPos blockPos) {
        return mc.blocks.getBlockByID(getBlockID(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    /**
     * @param entity the given entity
     * @return the chunk that contains the entity
     */
    public ChunkBase getChunkFor(@NotNull Entity entity) {
        Vec2i chunkPos = getChunkOrigin(entity.posX, entity.posZ);
        return getChunkAtOrigin(chunkPos.getX(), chunkPos.getY());
    }

    /**
     * @param x x component of the position vector
     * @param z y component of the position vector
     * @return the chunk that handles the square coordinate range of CHUNK_SIZE by CHUNK_SIZE that contains the given coordinate vector
     */
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
    public ChunkBase getChunkAtOrigin(int originX, int originZ) {
        return this.worldChunkHandler.getChunkAt(originX, originZ);
    }

    @NotNull
    public WorldChunkHandlerBase getWorldChunkHandler() {
        return worldChunkHandler;
    }

    /**
     * @param blockID the given blockID
     * @return true if the block with the given id is has transparency or is air.
     */
    public boolean canSeeThrough(int blockID) {
        Block block = mc.blocks.getBlockByID(blockID);
        return block == null || block.transparent;
    }

    /**
     * Stops all async work the world is doing
     */
    @Override
    public void stopAsyncWork() {
        this.worldThread.interrupt();
    }

    public ChunkLoaderBase getChunkLoader() {
        return chunkLoader;
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
    }

    public interface OnServerChunkCreationListener extends OnChunkCreationListener {
    }

    public WorldBase addOnChunkCreationListener(@Nullable OnServerChunkCreationListener onChunkCreationListener) {
        this.onChunkCreationListeners.add(onChunkCreationListener);
        return this;
    }

    @NotNull
    public List<OnServerChunkCreationListener> getOnChunkCreationListeners() {
        return onChunkCreationListeners;
    }
}
