package me.gommeantilegit.minecraft.world.chunk;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;

import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.profiler.Profiler;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.util.collections.ThreadBoundList;
import me.gommeantilegit.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class Chunk implements Tickable {

    /**
     * Width and height of the chunk region starting at {@link #x}, {@link #z}
     */
    public static final int CHUNK_SIZE = 8;

    /**
     * Chunk height
     */
    private final int height;

    /**
     * Start position of the region handled by the chunk
     */
    private final int x, z;

    /**
     * Parent world instance
     */

    @NotNull
    private final World world;

    /**
     * The mesh of the chunk being rendered.
     */
    @Nullable
    private Mesh mesh;

    /**
     * State if the chunk should be rebuilt the next time it is rendered.
     */
    private boolean rebuild = true;

    /**
     * List storing all entities of the chunk.
     */
    private final ThreadBoundList<Entity> entities = new ThreadBoundList<>(Minecraft.mc.minecraftThread);

    /**
     * State if the chunk is currently loaded (in render distance)
     *
     * @see me.gommeantilegit.minecraft.world.renderer.WorldRenderer#renderDistance
     */
    private boolean loaded = false;

    /**
     * A unique id of the current chunk.
     */
    private final long id = new Random().nextLong();

    /**
     * Three dimensional array of blockStates storing information about all block states.
     */
    @NotNull
    public final IBlockState[][][] blockStates;

    /**
     * Queue of Entities to be added to the chunks entity list on thread minecraft thread.
     */
    @NotNull
    private Queue<Entity> scheduledEntities = new LinkedList<>();

    /**
     * State whether or not entities are currently scheduled to be added to the chunks entities list.
     */
    private boolean entitiesScheduled = false;

    @NotNull
    private final BoundingBox boundingBox;

    /**
     * Default constructor of a Chunk object
     *
     * @param height height of the world -> becomes chunk height
     * @param x      startX position where the region managed by the chunk starts
     * @param z      startZ position where the region managed by the chunk starts
     * @param world  the parent world
     */
    public Chunk(int height, int x, int z, @NotNull World world) {
        this.height = height;
        this.x = x;
        this.z = z;
        this.world = world;
        this.blockStates = new IBlockState[CHUNK_SIZE][height][CHUNK_SIZE];
        this.boundingBox = new BoundingBox(new Vector3(x, 0, z), new Vector3(x + CHUNK_SIZE, height, z + CHUNK_SIZE));
    }

    /**
     * Updating the chunks entities.
     *
     * @param partialTicks the ticks to be performed in the current frame being rendered.
     */
    @Override
    public void tick(float partialTicks) {
        this.updateScheduledEntities();
        this.trackEntities();
    }

    /**
     * Updates {@link #scheduledEntities}.
     */
    private void updateScheduledEntities() {

        if (entitiesScheduled) {

            while (!this.scheduledEntities.isEmpty()) {
                try {

                    Entity entity = this.scheduledEntities.remove();
                    forceAddEntity(entity);
                } catch (NoSuchElementException ignored) {
                }
            }
            entitiesScheduled = false;
        }
    }

    /**
     * Profiler for measuring the performance of the {@link #rebuild()} method
     */
    public static final Profiler CHUNK_REBUILD_PROFILER = new Profiler("Chunk-rebuild", false);

    /**
     * Rebuilds the chunk mesh.
     */
    public void rebuild() {
        CHUNK_REBUILD_PROFILER.actionStart();
        MeshBuilder builder = buildChunkMesh();
        if (this.mesh != null) this.mesh.dispose();
        this.mesh = builder.end();
        CHUNK_REBUILD_PROFILER.actionEnd();
        CHUNK_REBUILD_PROFILER.printResults();
    }

    /**
     * @return a mesh-builder with the stored chunk in it.
     */
    public MeshBuilder buildChunkMesh() {
        MeshBuilder builder = new MeshBuilder();
        builder.ensureCapacity(
                (CHUNK_SIZE * CHUNK_SIZE * height * 4 * 4) / 2,
                (CHUNK_SIZE * CHUNK_SIZE * height * 4 * 6) / 2
        );
        builder.begin(Minecraft.mc.vertexAttributes, GL_TRIANGLES);
        for (int x = this.x; x < this.x + CHUNK_SIZE; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = this.z; z < this.z + CHUNK_SIZE; z++) {
                    IBlockState blockState = getBlockState(x, y, z);
                    if (blockState != null && blockState.getBlock() != null) {
                        blockState.getBlock().render(builder, x, y, z, world, blockState, false);
                    }
                }
            }
        }
        return builder;
    }

    /**
     * Renders the given chunk.
     *
     * @param partialTicks delta time
     */
    public void render(float partialTicks) {
        if (mesh != null)
            this.mesh.render(Minecraft.mc.shaderManager.stdShader, GL_TRIANGLES);

        this.renderEntities(partialTicks);

//        ShaderManager shaderManager = Minecraft.mc.shaderManager;
//        Minecraft.mc.shaderManager.stdShader.translate(0, 0, 0);
//        System.out.println(shaderManager.stdShader.getTranslateX() + ", " + shaderManager.stdShader.getTranslateY() + ", " + shaderManager.stdShader.getTranslateZ());

    }

    /**
     * Sets the state if the chunk should be rebuilt next time it is rendered.
     *
     * @param rebuild state if the chunk should be rebuilt.
     * @return self instance (Builder function)
     */
    public Chunk setNeedsRebuild(boolean rebuild) {
        this.rebuild = rebuild;
        return this;
    }

    /**
     * Adds the given entity to the chunks entity list.<br>
     * NOTE: MINECRAFT THREAD ONLY
     *
     * @param entity
     */
    public void forceAddEntity(@NotNull Entity entity) {
        this.entities.add(entity);
    }

    /**
     * Renders all entities
     *
     * @param partialTicks this performed this frame
     */
    private void renderEntities(float partialTicks) {
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            if (entity != null)
                entity.render(partialTicks);
        }
    }

    /**
     * Updating entities
     * > Removes dead entities and updates them. <br>
     * > Transfers entities to a new chunk, if the entity moves out of the chunk.
     */
    private void trackEntities() {
        //Tracking loaded entities
        for (int i = 0; i < this.entities.size(); i++) {
            Entity entity = entities.get(i);

            if (entity.skipUpdateTicks > 0)
                entity.skipUpdateTicks--;
            else {
                long currentTick = Minecraft.mc.timer.performedTicks;
                if (entity.lastUpdated != currentTick) {
                    entity.tick();
                    entity.lastUpdated = currentTick;
                }
            }

            if (entity.isInChunk(this)) {
                //Entity is still in the same chunk.
                if (entity.dead) {
                    entity.onDeath();
                    entities.remove(i--);
                }
            } else {
                //Entity has moved to another chunk
                Chunk newChunk = world.getChunkAt((int) entity.posX, (int) entity.posZ);

                //Keeping the entity in the old chunk, if the chunk is indeed null.
                if (newChunk != null) {
                    entity.onChunkChanged(newChunk, this);
                    entities.remove(i--);
                    newChunk.forceAddEntity(entity);
                }
            }
        }
    }

    /**
     * @param axisAlignedBB the boundingBox
     * @return if an entity is in the given bounding box
     */
    public boolean isFree(AxisAlignedBB axisAlignedBB) {
        for (int i = 0; i < this.entities.size(); i++) {
            Entity entity = entities.get(i);
            if (entity.boundingBox.intersects(axisAlignedBB)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the state if the chunk should be rebuilt.
     */
    public boolean needsRebuild() {
        return rebuild;
    }

    /**
     * @param x block x position
     * @param z block z position
     * @return the state if the given block coordinate is managed by the chunk instance.
     */
    public boolean contains(int x, int z) {
        return x >= this.x && z >= this.z && x < this.x + CHUNK_SIZE && z < this.z + CHUNK_SIZE;
    }

    /**
     * Loads the chunk. The chunk is now being rendered and updated on tick.
     * Minecraft Thread Only!
     */
    public void load() {
        this.world.getChunkLoader().scheduleLoad(this);
    }

    public Chunk setMesh(@Nullable Mesh mesh) {
        this.mesh = mesh;
        return this;
    }

    /**
     * Unloads the chunks. The chunk is no longer being rendered and updated on tick.
     * The chunk will stay loaded, if the chunk resists to be unloaded on request
     */
    public void unload() {
        this.world.getChunkLoader().scheduleUnload(this);
    }

    /**
     * Sets the block state for the given world absolute coordinate. (WITHOUT SCHEDULING THE ACTION ON THE {@link me.gommeantilegit.minecraft.world.block.change.WorldBlockChanger} INSTANCE)
     * USAGE IS DANGEROUS!!!
     *
     * @param x          world x position
     * @param y          world y position
     * @param z          world z position
     * @param blockState the new block state
     * @throws IllegalStateException if the specified coordinates are not managed by the chunk.
     */
    public void setBlockWithoutWorldBlockChangerObject(int x, int y, int z, @Nullable IBlockState blockState) {
        changeBlock(x, y, z, blockState);
    }

    /**
     * Sets the block state for the given world absolute coordinate. (Schedules an action on {@link me.gommeantilegit.minecraft.world.block.change.WorldBlockChanger} INSTANCE)
     *
     * @param x             world x position
     * @param y             world y position
     * @param z             world z position
     * @param newBlockState the new block state
     * @throws IllegalStateException if the specified coordinates are not managed by the chunk.
     */
    public void setBlock(int x, int y, int z, @Nullable IBlockState newBlockState) {
        world.getBlockChanger().blockChange(x, y, z, newBlockState, true, this);
    }

    /**
     * Changes the block at the specified position to the new type
     *
     * @param x          x coordinate
     * @param y          y coordinate
     * @param z          z coordinate
     * @param blockState the new block state
     */
    private void changeBlock(int x, int y, int z, @Nullable IBlockState blockState) {
        int relX = x - this.x;
        int relY = max(0, y);
        int relZ = z - this.z;
        if (!this.contains(x, z))
            throw new IllegalStateException("Couldn't set block state to blockState: " + blockState + ". Coordinates x: " + x + ", y: " + y + ", z: " + z + "; relX: " + relX + ", relY: " + relY + ", relZ: " + relZ + "; are not contained in Chunk: [" + this.toString() + "]");
        this.blockStates[relX][relY][relZ] = blockState;
        this.setNeedsRebuild(true);
    }

    /**
     * Sets the block at a given position to block id
     * WITHOUT SCHEDULING AN ACTION ON THE BLOCK CHANGER OF THE WORLD {@link me.gommeantilegit.minecraft.world.block.change.WorldBlockChanger}.
     * DANGEROUS!
     * ALSO: USE ONLY IN WORLD GENERATION
     *
     * @param x          x coordinate
     * @param y          y coordinate
     * @param z          z coordinate
     * @param blockState the new block state
     */
    public void setBlockNoChangeWithoutWorldBlockChangerObject(int x, int y, int z, @Nullable IBlockState blockState) {
        changeBlock(x, y, z, blockState);
    }

    /**
     * Sets the block at a given position to block id
     *
     * @param x          x coordinate
     * @param y          y coordinate
     * @param z          z coordinate
     * @param blockState the new block state
     */
    public void setBlockNoChange(int x, int y, int z, @Nullable IBlockState blockState) {
        world.getBlockChanger().blockChange(x, y, z, blockState, false, this);
    }

    /**
     * @param x world x coordinate
     * @param y world y coordinate
     * @param z world z coordinate
     * @return the id of the block at the given absolute world coordinates
     * @throws IllegalStateException if the specified coordinates are not managed by the chunk.
     */
    public int getBlockIDAt(int x, int y, int z) {
        Block block = getBlockAt(x, y, z);
        if (block == null) return 0;
        else return block.getId();
    }

    /**
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return the block instance of the block state at the specified coordinate or null if block state is null indicating that the block is air.
     */
    @Nullable
    public Block getBlockAt(int x, int y, int z) {
        IBlockState blockState = getBlockState(x, y, z);
        if (blockState == null)
            return null;
        else return blockState.getBlock();
    }

    /**
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return the block state at the specified coordinate
     */
    @Nullable
    public IBlockState getBlockState(int x, int y, int z) {
        if (y < 0)
            return null; //Returning air, if the block pos is below the world
        int relX = x - this.x;
        int relY = y;
        int relZ = z - this.z;
        if (!this.contains(x, z))
            throw new IllegalStateException("Couldn't get block state of coordinates x: " + x + ", y: " + y + ", z:" + z + "; relX: " + relX + ", relY: " + relY + ", relZ: " + relZ + "; are not contained in Chunk: [" + this.toString() + "]");
        return blockStates[relX][relY][relZ];
    }

    /**
     * @return the true if all entities allow the scheduleUnload of this chunk
     */
    public boolean checkForUnload() {
        for (Entity entity : this.entities) {
            if (!entity.allowChunkUnload(this)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Nullifies the variable {@link #mesh}
     */
    private void nullifyMesh() {
        this.mesh = null;
    }

    public int getHeight() {
        return height;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    @Nullable
    public Mesh getMesh() {
        return mesh;
    }

    public boolean isRebuild() {
        return rebuild;
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public String toString() {
        return "{loaded: " + this.loaded + ", entities: " + this.entities.size() + ", x: " + this.x + ", z: " + this.z + ", height: " + this.height + ", id: " + id + "}";
    }

    /**
     * @return a new Vector storing the values of {@link #x} and {@link #z}
     * Note that changes made to this instance do not affect the chunk in any way.
     */
    public Vector2 getChunkOrigin() {
        return new Vector2(x, z);
    }

    /**
     * Schedules an entity to be added to the
     *
     * @param entity the entity to be added
     */
    public void scheduleAddEntity(@NotNull Entity entity) {
        this.scheduledEntities.add(entity);
        this.entitiesScheduled = true;
    }

    /**
     * @param region    the region
     * @param predicate the condition the entity must fulfill
     * @return a collection of entities that are in the given region and fulfill the requirements of the predicate.
     */
    public Collection<? extends Entity> getEntitiesWithinAABBForEntity(AxisAlignedBB region, Predicate<? super Entity> predicate) {
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : this.entities) {
            if (entity.getBoundingBox().intersects(region) && predicate.test(entity)) {
                entities.add(entity);
            }
        }
        return entities;
    }

    /**
     * Sets {@link #loaded} to state
     *
     * @param state the new state of loaded
     */
    public void setLoaded(boolean state) {
        this.loaded = state;
    }

    /**
     * @return the chunks bounding-box
     */
    @NotNull
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }
}
