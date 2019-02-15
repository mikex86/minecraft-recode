package me.gommeantilegit.minecraft.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.timer.Timer;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.util.collections.ThreadBoundList;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.block.change.BlockChange;
import me.gommeantilegit.minecraft.world.block.change.WorldBlockChanger;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import me.gommeantilegit.minecraft.world.chunk.creator.ChunkCreator;
import me.gommeantilegit.minecraft.world.chunk.loader.ChunkLoader;
import me.gommeantilegit.minecraft.world.chunk.world.WorldChunkHandler;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.renderer.WorldRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.Math.*;
import static me.gommeantilegit.minecraft.world.chunk.Chunk.CHUNK_SIZE;

public class World implements Tickable, Runnable, AsyncOperation, OpenGLOperation {

    /**
     * The player viewing the world
     */
    @NotNull
    public final Player viewer;

    /**
     * Object handling block state changes
     */
    @NotNull
    private final WorldBlockChanger blockChanger;

    /**
     * World Async Thread for dirty work
     */
    @NotNull
    private final Timer worldThreadTimer = new Timer(20.0f);

    /**
     * World Generator
     */
    @NotNull
    private final WorldGenerator worldGenerator;

    /**
     * State if the world is fully operational
     */
    private boolean isLoaded = false;

    /**
     * Object capable of loading and unloading chunks according to {@link WorldRenderer#renderDistance}
     */
    @NotNull
    public final ChunkLoader chunkLoader;

    /**
     * List of all changes that were made to the originally generated world by the WorldGenerator.
     */
    @NotNull
    public List<BlockChange> getBlockChanges() {
        return blockChanges;
    }

    /**
     * Object capable of creating the chunks to be later loaded and built
     */
    @NotNull
    private final ChunkCreator chunkCreator;

    /**
     * WorldChunkHandler Object storing world chunk lists
     */
    @NotNull
    private final WorldChunkHandler worldChunkHandler = new WorldChunkHandler();

    /**
     * Default {@link OnChunkCreationListener} invoked on chunk creation.
     *
     * @see OnChunkCreationListener
     * @see OnChunkCreationListener#onChunkCreated(Chunk)
     */
    @Nullable
    private OnChunkCreationListener onChunkCreationListener = null;

    /**
     * Object for rendering the world
     */
    @NotNull
    public final WorldRenderer worldRenderer;

    /**
     * The world's thread for async operations.
     */
    @NotNull
    private final Thread worldThread;

    /**
     * Height of the world
     */
    public final int height = 256;

    /**
     * List storing all block changes
     */
    @NotNull
    private final List<BlockChange> blockChanges;

    /**
     * Default world constructor
     *
     * @param viewer         the player viewing the world
     * @param worldGenerator world generator for world generation
     * @param blockChanges   block changes
     */
    public World(@NotNull Player viewer, @NotNull WorldGenerator worldGenerator, @NotNull List<BlockChange> blockChanges) {
        this.viewer = viewer;
        this.worldGenerator = worldGenerator;
        this.blockChanges = blockChanges;
        this.worldRenderer = new WorldRenderer(this, viewer); // Must be initialized before #chunkLoader
        this.chunkLoader = new ChunkLoader(this); // Must be initialized after #worldRenderer
        this.chunkCreator = new ChunkCreator(this);
        this.blockChanger = new WorldBlockChanger(this);
        this.worldThread = new Thread(this, "World-thread");
        this.worldThread.start();
        this.setOnChunkCreationListener(worldGenerator);
    }

    /**
     * Spawns the given entity into the world
     *
     * @param entity the given entity.
     */
    public void spawnEntityInWorld(Entity entity) {
        entity.setWorld(this);
        chunkCreator.submit(entity); // Submitting the entity to the chunk creator
    }

    /**
     * World onTick update
     */
    @Override
    public void tick(float partialTicks) {
        if (!isLoaded)
            return;

        for (Chunk chunk : this.worldChunkHandler.getChunks()) {
            if (chunk.isLoaded())
                chunk.tick(partialTicks);
        }

        this.chunkCreator.tick(partialTicks);
        this.chunkLoader.tick(partialTicks);
    }

    @Override
    public void onOpenGLContext(float partialTicks) {
        this.chunkLoader.chunkRebuilder.onOpenGLContext(partialTicks);
    }

    /**
     * Stops all asynchronous processes of the world.
     */
    public void stop() {
        this.worldThread.interrupt();
    }

    /**
     * @param axisAlignedBB the boundingBox
     * @return if an entity is in the given bounding box
     */
    public boolean isFree(AxisAlignedBB axisAlignedBB) {
        List<Chunk> chunks = getChunksInBoundingBox(axisAlignedBB);
        for (Chunk chunk : chunks) {
            if (!chunk.isFree(axisAlignedBB))
                return false;
        }
        return true;
    }


    /**
     * @param axisAlignedBB the bounding box.
     * @return chunks managing parts of the region.
     */
    @NotNull
    private List<Chunk> getChunksInBoundingBox(@NotNull AxisAlignedBB axisAlignedBB) {
        List<Chunk> chunks = new ArrayList<>(4);
        float x0 = axisAlignedBB.x0;
        float z0 = axisAlignedBB.z0;
        float x1 = axisAlignedBB.x1;
        float z1 = axisAlignedBB.z1;
        {
            Chunk chunk = getChunkAt((int) x0, round(z0));
            assert chunk != null;
            chunks.add(chunk);
        }
        {
            Chunk chunk = getChunkAt((int) x0, round(z1));
            assert chunk != null;
            if (!chunks.contains(chunk))
                chunks.add(chunk);
        }
        {
            Chunk chunk = getChunkAt((int) x1, (int) z1);
            assert chunk != null;
            if (!chunks.contains(chunk))
                chunks.add(chunk);
        }
        {
            Chunk chunk = getChunkAt((int) x1, (int) z0);
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
        Block block = Blocks.getBlockByID(getBlockID(x, y, z));
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
                        if (block != null && (axisAlignedBB2 = block.getBoundingBox(this, bp, state)) != null) {
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
        return Blocks.getBlockByID(getBlockID(x, y, z));
    }

    /**
     * @param blockID the given blockID
     * @return true if the block with the given id is has transparency or is air.
     */
    public boolean canSeeThrough(int blockID) {
        Block block = Blocks.getBlockByID(blockID);
        return block == null || block.transparent;
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
        Chunk chunk = getChunkAt(x, z);
        if (chunk == null)
            throw new IllegalStateException("Couldn't set block state of coordinates [x: " + x + ", y:" + y + ", " + z + "] to blockID: " + newBlockState + ". No Chunk containing coordinates.");
        chunk.setBlock(x, y, z, newBlockState);
    }

    /**
     * Rebuilds all chunks partially rendering the block at the specified position
     *
     * @param x blockX coordinate
     * @param z blockZ coordinate
     */
    public void rebuildChunksFor(int x, int z) {
        ArrayList<Chunk> chunks = new ArrayList<>();

        for (int xo = -1; xo <= 1; xo++) {
            int zo;
            if (xo == 0)
                for (zo = -1; zo <= 1; zo++) {
                    Chunk chunk = getChunkAt(x + xo, z + zo);
                    if (chunk != null && !chunks.contains(chunk)) chunks.add(chunk);
                }
            else {
                zo = 0;
                Chunk chunk = getChunkAt(x + xo, z + zo);
                if (chunk != null && !chunks.contains(chunk)) chunks.add(chunk);
            }
        }

        for (Chunk chunk : chunks) {
            chunk.setNeedsRebuild(true);
            if (!chunk.isLoaded())
                chunk.load();
        }
    }

    /**
     * @param x x position
     * @param z z position
     * @return the parent chunk of the given position.
     */
    @Nullable
    public Chunk getChunkAt(int x, int z) {
        ThreadBoundList<Chunk> chunks = this.worldChunkHandler.getChunks();
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            if (chunk.contains(x, z)) return chunk;
        }
        ThreadBoundList<Chunk> chunks1 = this.worldChunkHandler.getChunks();
        for (int i = 0; i < chunks1.size(); i++) {
            Chunk chunk = chunks1.get(i);
            if (chunk.contains(x, z)) return chunk;
        }
        return null;
    }

    /**
     * Applies changes from {@link #blockChanges} for this chunk to the loaded chunk
     *
     * @param chunk the chunk that is currently loaded
     */
    public void applyBlockChanges(@NotNull Chunk chunk) {
        for (BlockChange blockChange : blockChanges) {
            BlockPos bp = blockChange.getPosition();
            if (chunk.contains(bp.getX(), bp.getZ())) {
                chunk.setBlockNoChangeWithoutWorldBlockChangerObject(bp.getX(), bp.getY(), bp.getZ(), blockChange.getNewBlockState());
            }
        }
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
        if (blockState.getBlock() == null)
            return 0;
        else return blockState.getBlock().getId();
    }

    @Nullable
    public Block getBlock(@NotNull BlockPos blockPos) {
        return Blocks.getBlockByID(getBlockID(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    /**
     * Rebuilds all chunks
     */
    public void rebuildAllChunks() {
        for (Chunk chunk : this.worldChunkHandler.getChunks()) {
            chunk.rebuild();
        }
    }

    public List<Entity> getEntitiesInAABBexcluding(Entity entityIn, AxisAlignedBB boundingBox, Predicate<? super Entity> predicate) {
        List<Entity> list = new ArrayList<>();

        float rawX0 = (boundingBox.x0 - 2.0f);
        float rawX1 = (boundingBox.x1 + 2.0f);

        float rawZ0 = (boundingBox.z0 - 2.0f);
        float rawZ1 = (boundingBox.z1 + 2.0f);

        Vec2i chunkOrigin0 = getChunkOrigin(rawX0, rawZ0);
        Vec2i chunkOrigin1 = getChunkOrigin(rawX1, rawZ1);

        int x0 = chunkOrigin0.getX();
        int z0 = chunkOrigin0.getY();

        int x1 = chunkOrigin1.getX();
        int z1 = chunkOrigin1.getY();

        for (int x = x0; x <= x1; ++x) {
            for (int z = z0; z <= z1; ++z) {
                Chunk chunk = getChunkAt(x, z);
                if (chunk != null && chunk.isLoaded()) {
                    list.addAll(chunk.getEntitiesWithinAABBForEntity(boundingBox, predicate));
                }
            }
        }

        return list;
    }

    /**
     * Renders the world by calling {@link WorldRenderer#render(float)}
     *
     * @param partialTicks amount of ticks performed in the current frame
     */
    public void render(float partialTicks) {
        this.worldRenderer.render(partialTicks);
    }

    /**
     * Invalidates all meshes of the chunks
     */
    public void invalidateMeshes() {
        for (Chunk chunk : this.worldChunkHandler.getChunks()) {
            if (chunk.getMesh() != null)
                chunk.getMesh().dispose();
            chunk.setNeedsRebuild(true);
        }
    }

    /**
     * Method on async world thread
     */
    @Override
    public void run() {
        try {
            while (Minecraft.mc.isRunning()) {
                worldThreadTimer.advanceTime();
                for (int i = 0; i < worldThreadTimer.ticks; i++) {
                    worldThreadTimer.tick(worldThreadTimer.partialTicks);
                    onAsyncThread();
                }
                Thread.sleep(10);
            }
            System.out.println("World async thread terminated.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Invoked by the world thread
     */
    @Override
    public void onAsyncThread() {
        this.blockChanger.onAsyncThread();
        this.chunkCreator.onAsyncThread();
        this.chunkLoader.onAsyncThread();
    }

    /**
     * @param entity the given entity
     * @return the chunk that contains the entity
     */
    public Chunk getChunkFor(@NotNull Entity entity) {
        Vec2i chunkPos = getChunkOrigin(entity.posX, entity.posZ);
        return getChunkAt(chunkPos.getX(), chunkPos.getY());
    }

    /**
     * @param x x component of the position vector
     * @param z y component of the position vector
     * @return the chunk that handles the square coordinate range of CHUNK_SIZE by CHUNK_SIZE that contains the given coordinate vector
     */
    public Chunk getChunkFor(float x, float z) {
        Vec2i chunkPos = getChunkOrigin(x, z);
        return getChunkAt(chunkPos.getX(), chunkPos.getY());
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
     * Adds a blockChange instance to {@link World#blockChanges} if none exists for this coordinate or adjusts its block value if one is already present.
     * If the block change returns to it original state, the element is removed.
     * IMPORTANT: NEEDS TO BE PERFORMED BEFORE BLOCK CHANGE IN WORLD
     *
     * @param x          x coordinate
     * @param y          y coordinate
     * @param z          z coordinate
     * @param blockState the new block state
     */
    public void setBlockChanged(int x, int y, int z, @Nullable IBlockState blockState) {
        BlockPos bp = new BlockPos(x, y, z);
        BlockChange change = getBlockChangeFor(bp);
        if (change == null) {
            blockChanges.add(new BlockChange(new BlockPos(x, y, z), blockState, getBlockState(x, y, z)));
        } else {
            if (blockState == change.getOriginalBlockState()) {
                this.blockChanges.remove(change);
            } else {
                change.setNewBlock(blockState);
            }
        }
    }

    @Nullable
    public IBlockState getBlockState(int x, int y, int z) {
        Chunk chunk = this.getChunkAt(x, z);
        if (chunk == null) return null;
        else return chunk.getBlockState(x, y, z);
    }

    @Nullable
    public IBlockState getBlockState(@NotNull BlockPos blockPos) {
        return getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    /**
     * @param bp the specified position
     * @return the BlockChange instance for the given position if present. Else null.
     */
    @Nullable
    private BlockChange getBlockChangeFor(@NotNull BlockPos bp) {
        for (BlockChange blockChange : this.blockChanges) {
            if (blockChange.getPosition().equals(bp)) {
                return blockChange;
            }
        }
        return null;
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
     * Object representing a listener listening to the creation of new chunks
     */
    public interface OnChunkCreationListener {
        /**
         * Called when a chunks is created and added to the world
         *
         * @param chunk the chunk, that is added
         */
        void onChunkCreated(@NotNull Chunk chunk);

    }

    public World setOnChunkCreationListener(@Nullable OnChunkCreationListener onChunkCreationListener) {
        this.onChunkCreationListener = onChunkCreationListener;
        return this;
    }

    public World setLoaded(boolean loaded) {
        isLoaded = loaded;
        return this;
    }

    @NotNull
    public WorldBlockChanger getBlockChanger() {
        return blockChanger;
    }

    @NotNull
    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
    }

    @NotNull
    public WorldGenerator getWorldGenerator() {
        return worldGenerator;
    }

    @NotNull
    public ChunkLoader getChunkLoader() {
        return chunkLoader;
    }

    @NotNull
    public WorldChunkHandler getWorldChunkHandler() {
        return worldChunkHandler;
    }

    @NotNull
    public ChunkCreator getChunkCreator() {
        return chunkCreator;
    }

    @Nullable
    public OnChunkCreationListener getOnChunkCreationListener() {
        return onChunkCreationListener;
    }
}
