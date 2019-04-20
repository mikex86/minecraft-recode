package me.gommeantilegit.minecraft.world.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.annotations.Unsafe;
import me.gommeantilegit.minecraft.block.BlockBase;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.block.state.BlockStateBase;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.utils.collections.ThreadBoundList;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.utils.serialization.exception.DeserializationException;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.block.change.WorldBlockChangerBase;
import me.gommeantilegit.minecraft.world.chunk.loader.ChunkLoaderBase;
import me.gommeantilegit.minecraft.world.chunk.world.WorldChunkHandlerBase;
import me.gommeantilegit.minecraft.world.saveformat.data.ChunkData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;

import static java.lang.Math.max;
import static me.gommeantilegit.minecraft.world.chunk.ChunkSection.CHUNK_SECTION_SIZE;


public abstract class ChunkBase<CS extends ChunkSection<CB>, CB extends ChunkBase<CS, CB, CH, BLOCKS, MC, BB, BS, WB, CL>, CH extends WorldChunkHandlerBase<CB, MC, BB, BLOCKS, BS>, BLOCKS extends Blocks<BB, MC>, MC extends AbstractMinecraft<BB, MC, BLOCKS, BS>, BB extends BlockBase<MC, BB, BS, BLOCKS>, BS extends BlockStateBase<BB>, WB extends WorldBase<CL, BLOCKS, MC, WB, BS, CB, CH, BB, ?>, CL extends ChunkLoaderBase<CB, WB>> implements Tickable {

    /**
     * Width and height of the chunk region starting at {@link #x}, {@link #z}
     */
    public static final int CHUNK_SIZE = 16;

    /**
     * ChunkBase height
     */
    protected final int height;

    /**
     * Start position of the region handled by the chunk
     */
    protected final int x, z;

    /**
     * Parent world instance
     */
    @NotNull
    protected final WB world;

    /**
     * State if the chunk is currently loaded (tick updated)
     */
    protected boolean loaded = false;

    /**
     * List storing all entities of the chunk.
     */
    protected final ArrayList<Entity> entities;

    /**
     * A unique id of the current chunk.
     */
    protected final long id = new Random().nextLong();

    /**
     * Three dimensional array of blockStates storing information about all block states.
     */
    @NotNull
    public final BS[][][] blockStates;

    /**
     * Chunk bounding box for frustum culling
     */
    @NotNull
    private final BoundingBox boundingBox;

    /**
     * Minecraft instance
     */
    @NotNull
    public final MC mc;

    /**
     * Array of chunk sections
     */
    @NotNull
    private final CS[] chunkSections;

    /**
     * Default constructor of a ChunkBase object
     *
     * @param height              height of the world -> becomes chunk height
     * @param x                   startX position where the region managed by the chunk starts
     * @param z                   startZ position where the region managed by the chunk starts
     * @param world               the parent world
     * @param blockStateBaseClass class of blockStateBase type BS
     * @param chunkSectionsClass  class of chunkSectionBase type CS
     */
    public ChunkBase(int height, int x, int z, @NotNull WB world, Class<BS> blockStateBaseClass, @NotNull Class<CS> chunkSectionsClass) {
        this.height = height;
        this.x = x;
        this.z = z;
        this.world = world;
        this.blockStates = (BS[][][]) Array.newInstance(blockStateBaseClass, CHUNK_SIZE, height, CHUNK_SIZE);
        this.boundingBox = new BoundingBox(new Vector3(x, 0, z), new Vector3(x + CHUNK_SIZE, height, z + CHUNK_SIZE));
        this.mc = world.mc;
        this.entities = new ArrayList<>();
        this.chunkSections = (CS[]) Array.newInstance(chunkSectionsClass, height / CHUNK_SECTION_SIZE);
        for (int i = 0; i < chunkSections.length; i++) {
            chunkSections[i] = getChunkSection(i * CHUNK_SECTION_SIZE);
        }
    }

    /**
     * @param startHeight the y coordinate where the chunk section returned should start
     * @return the chunk section instance to be stored in {@link #chunkSections} during object init.
     */
    @NotNull
    protected abstract CS getChunkSection(int startHeight);

    /**
     * Updating the chunks entities.
     *
     * @param partialTicks the ticks to be performed in the current frame being rendered.
     */
    @Override
    public void tick(float partialTicks) {
        this.trackEntities();
    }

    /**
     * Adds the given entity to the chunks entity list without synchronization! <br>
     * NOTE: MINECRAFT THREAD ONLY
     *
     * @param entity the entity to add
     */
    public void forceAddEntity(@NotNull Entity entity) {
        entity.setWorld(world);
        entity.onChunkChanged(this, null);
        this.entities.add(entity);
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
                long currentTick = mc.timer.performedTicks;
                if (entity.lastUpdated != currentTick) {
                    entity.tick();
                    entity.lastUpdated = currentTick;
                }
            }

            if (entity.isInChunk(this) || entity.chunkChanged) {
                //Entity is still in the same chunk.
                if (entity.dead) {
                    entity.onDeath();
                    removeEntity(i--, EntityRemoveReason.ENTITY_DEATH);
                }
                entity.chunkChanged = false;
            } else {
                //Entity has moved to another chunk
                ChunkBase newChunk = world.getChunkFor(entity);

                //Keeping the entity in the old chunk, if the chunk is indeed null.
                if (newChunk != null) {
                    entity.onChunkChanged(newChunk, this);
                    removeEntity(i--, EntityRemoveReason.ENTITY_CHUNK_CHANGED);
                    newChunk.forceAddEntity(entity);
                }
            }
        }
    }

    /**
     * The method that removes a given entity
     *
     * @param entityIndex  the index of the entity to be removed in the {@link #entities} list
     * @param removeReason the reason the entity was removed from the list
     */
    protected void removeEntity(int entityIndex, @NotNull EntityRemoveReason removeReason) {
        this.entities.remove(entityIndex);
    }

    /**
     * @param axisAlignedBB the boundingBox
     * @return if an entity is in the given bounding box
     */
    public boolean isFree(AxisAlignedBB axisAlignedBB) {
        ArrayList<Entity> entities = this.entities;
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            if (entity.boundingBox.intersects(axisAlignedBB)) {
                return false;
            }
        }
        return true;
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
     * Also tells the chunk cache to un-optimize the chunks data to get quicker access time to the element.
     * All this happens asynchronous. Invocation of this method will be quick.
     */
    public synchronized void load() {
        if(!loaded) {
            world.getWorldChunkHandler().addLoadedChunk((CB) this);
            world.getWorldChunkHandler().removeUnloadedChunk((CB) this);
            setLoaded(true);
        }
    }

    /**
     * Unloads the chunks. The chunk is no longer being rendered and updated on tick.
     * The chunk will stay loaded, if the chunk resists to be unloaded on request.
     * Also tells the block state cache to optimize the chunks data.
     * All this happens asynchronous. Invocation of this method will be quick.
     */
    public synchronized void unload() {
        if(loaded) {
            world.getWorldChunkHandler().removeLoadedChunk((CB) this);
            world.getWorldChunkHandler().addUnloadedChunk((CB) this);
            setLoaded(false);
        }
    }

    /**
     * Sets the block state for the given world absolute coordinate. (WITHOUT SCHEDULING THE ACTION ON THE {@link WorldBlockChangerBase} INSTANCE)
     * USAGE IS DANGEROUS.
     *
     * @param x          world x position
     * @param y          world y position
     * @param z          world z position
     * @param blockState the new block state
     * @throws IllegalStateException if the specified coordinates are not managed by the chunk.
     */
    @Unsafe
    public void setBlockWithoutWorldBlockChangerObject(int x, int y, int z, @Nullable BS blockState) {
        changeBlock(x, y, z, blockState);
    }

    /**
     * Sets the block state for the given world absolute coordinate. (Schedules an action on {@link WorldBlockChangerBase} INSTANCE)
     *
     * @param x             world x position
     * @param y             world y position
     * @param z             world z position
     * @param newBlockState the new block state
     * @throws IllegalStateException if the specified coordinates are not managed by the chunk.
     */
    public void setBlock(int x, int y, int z, @Nullable BS newBlockState) {
        world.getBlockChanger().blockChange(x, y, z, newBlockState, (CB) this);
    }

    /**
     * Changes the block at the specified position to the new type
     *
     * @param x          x coordinate
     * @param y          y coordinate
     * @param z          z coordinate
     * @param blockState the new block state
     */
    @Unsafe
    protected void changeBlock(int x, int y, int z, @Nullable BS blockState) {
        int relX = x - this.x;
        int relY = max(0, y);
        int relZ = z - this.z;
        if (!this.contains(x, z))
            throw new IllegalStateException("Couldn't set block state to blockState: " + blockState + ". Coordinates x: " + x + ", y: " + y + ", z: " + z + "; relX: " + relX + ", relY: " + relY + ", relZ: " + relZ + "; are not contained in ChunkBase: [" + this.toString() + "]");
        this.blockStates[relX][relY][relZ] = blockState;
    }

    /**
     * Sets the block at a given position to block id
     *
     * @param x          x coordinate
     * @param y          y coordinate
     * @param z          z coordinate
     * @param blockState the new block state
     */
    public void setBlockNoChange(int x, int y, int z, @Nullable BS blockState) {
        world.getBlockChanger().blockChange(x, y, z, blockState, (CB) this);
    }

    /**
     * @param x world x coordinate
     * @param y world y coordinate
     * @param z world z coordinate
     * @return the id of the block at the given absolute world coordinates
     * @throws IllegalStateException if the specified coordinates are not managed by the chunk.
     */
    public int getBlockIDAt(int x, int y, int z) {
        BlockBase block = getBlockAt(x, y, z);
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
    public BB getBlockAt(int x, int y, int z) {
        BS blockState = getBlockState(x, y, z);
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
    public BS getBlockState(int x, int y, int z) {
        if (y < 0 || y >= height)
            return null; //Returning air, if the block pos is below the world
        int relX = x - this.x;
        int relZ = z - this.z;
        if (!this.contains(x, z))
            throw new IllegalStateException("Couldn't get block state of coordinates x: " + x + ", y: " + y + ", z:" + z + "; relX: " + relX + ", relY: " + y + ", relZ: " + relZ + "; are not contained in ChunkBase: [" + this.toString() + "]");
        return blockStates[relX][y][relZ];
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
     * Sets the chunks data accordingly to the information encoded in #chunkData.
     *
     * @param bytes             the data is the encoded chunk data for this chunk
     * @param chunkSectionsSent the states if the individual chunk sections have been sent in the packet
     */
    public void setChunkData(@NotNull byte[] bytes, @NotNull boolean[] chunkSectionsSent) {
        this.clearBlocks();
        BitByteBuffer buf = new BitByteBuffer(bytes, Integer.MAX_VALUE);
        try {
            ChunkData<BS> chunkData = mc.chunkSerializer.deserialize(buf, height, chunkSectionsSent);
            apply(chunkData);
        } catch (DeserializationException e) {
            mc.logger.crash("Cannot deserialize chunk data from byte array!", e);
        }
    }

    /**
     * Applies the chunk state of the specified {@link ChunkData} instance
     *
     * @param chunkData the data object storing the state that should be applied to this chunk
     */
    private void apply(@NotNull ChunkData<BS> chunkData) {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    this.blockStates[x][y][z] = chunkData.getBlockStates()[x][y][z];
                }
            }
        }
    }

    /**
     * Sets all blocks of the chunk to air
     */
    private void clearBlocks() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    this.blockStates[x][y][z] = null;
                }
            }
        }
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
    public WB getWorld() {
        return world;
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
     * @return a new 2d Vector storing the values of {@link #x} and {@link #z}
     * Note that changes made to this instance do not affect the chunk in any way.
     */
    public Vec2i getChunkOrigin() {
        return new Vec2i(x, z);
    }

    /**
     * Adds the given entity to the chunk's entity list
     *
     * @param entity the entity to be added
     */
    public void addEntity(@NotNull Entity entity) {
        synchronized (this.entities) {
            forceAddEntity(entity);
        }
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
    @Unsafe
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

    @NotNull
    public CS[] getChunkSections() {
        return chunkSections;
    }

    /**
     * Represents the reason why an entity has been remove from the list of entities of a given chunk
     */
    public enum EntityRemoveReason {
        /**
         * The reason that indicates that the entity died and was thus removed
         */
        ENTITY_DEATH,

        /**
         * The reason that indicates that the entity moved to another chunk and was thus removed
         */
        ENTITY_CHUNK_CHANGED
    }
}
