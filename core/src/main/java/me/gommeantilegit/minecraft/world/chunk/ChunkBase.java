package me.gommeantilegit.minecraft.world.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import kotlin.Pair;
import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.annotations.Unsafe;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.utils.serialization.exception.DeserializationException;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static me.gommeantilegit.minecraft.world.chunk.ChunkSection.CHUNK_SECTION_SIZE;


public class ChunkBase implements Tickable {

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
    protected final WorldBase world;

    /**
     * State if the chunk is currently loaded (tick updated)
     * (-1 -> initial state, 0 unloaded, 1 loaded)
     */
    protected int loadedState = -1;

    /**
     * List storing all entities of the chunk.
     */
    protected final List<Entity> entities;

    /**
     * A unique id of the current chunk.
     */
    protected final long id = new Random().nextLong();

    /**
     * Chunk bounding box for frustum culling
     */
    @NotNull
    private final BoundingBox boundingBox;

    /**
     * Minecraft instance
     */
    @NotNull
    public final AbstractMinecraft mc;

    /**
     * List of chunk sections
     */
    @NotNull
    private final List<ChunkSection> chunkSections;

//    /**
//     * 2D array storing a Biome id for a given x and z value.
//     */
//    @NotNull
//    private final byte[][] biome = new byte[CHUNK_SIZE][CHUNK_SIZE];

    /**
     * Threadsafe queue of entities (and respective data to perform the operation) that should be added to the entity list of the chunk on the next tick.
     */
    @NotNull
    private final Queue<Pair<Entity, Consumer<Entity>>> pendingEntitiesToAdd = new ConcurrentLinkedQueue<>();

    /**
     * Threadsafe queue of entities (and respective data to perform the operation) that should be removed to the entity list of the chunk on the next tick.
     */
    @NotNull
    private final Queue<Pair<Pair<Entity, EntityRemoveReason>, Consumer<Entity>>> pendingEntitiesToRemove = new ConcurrentLinkedQueue<>();

    private long nTicksPerformed;

    /**
     * Default constructor of a ChunkBase object
     *
     * @param height height of the world -> becomes chunk height
     * @param x      startX position where the region managed by the chunk starts
     * @param z      startZ position where the region managed by the chunk starts
     * @param world  the parent world
     */
    public ChunkBase(int height, int x, int z, @NotNull WorldBase world) {
        this.height = height;
        this.x = x;
        this.z = z;
        this.world = world;
        this.boundingBox = new BoundingBox(new Vector3(x, 0, z), new Vector3(x + CHUNK_SIZE, height, z + CHUNK_SIZE));
        this.mc = world.mc;
        this.entities = new ArrayList<>();
        this.chunkSections = new ArrayList<>(height / CHUNK_SECTION_SIZE);
        this.initChunkSections();
    }


    protected ChunkBase(int height, int x, int z, @NotNull WorldBase world, @NotNull List<ChunkSection> sections) {
        this.height = height;
        this.x = x;
        this.z = z;
        this.world = world;
        this.boundingBox = new BoundingBox(new Vector3(x, 0, z), new Vector3(x + CHUNK_SIZE, height, z + CHUNK_SIZE));
        this.mc = world.mc;
        this.entities = new ArrayList<>();
        this.chunkSections = sections;
    }

    protected void initChunkSections() {
        assert chunkSections.isEmpty();
        for (int i = 0; i < height / CHUNK_SECTION_SIZE; i++) {
            this.chunkSections.add(createChunkSection(i * CHUNK_SECTION_SIZE));
        }
    }

    /**
     * @param startHeight the y coordinate where the chunk section returned should start
     * @return the chunk section instance to be stored in {@link #chunkSections} during object init.
     */
    @NotNull
    protected ChunkSection createChunkSection(int startHeight) {
        return new ChunkSection(this, startHeight);
    }

    /**
     * Updating the chunks entities.
     *
     * @param partialTicks the ticks to be performed in the current frame being rendered.
     */
    @Override
    public void tick(float partialTicks) {
        this.updatePendingEntities();
        this.trackEntities();
        this.nTicksPerformed++;
    }

    private void updatePendingEntities() {
        while (!this.pendingEntitiesToAdd.isEmpty()) {
            Pair<Entity, Consumer<Entity>> pair = this.pendingEntitiesToAdd.remove();
            Entity entity = pair.getFirst();
            forceAddEntity(entity);
            Consumer<Entity> listener = pair.getSecond();
            if (listener != null) {
                listener.accept(entity);
            }
        }
        while (!this.pendingEntitiesToRemove.isEmpty()) {
            Pair<Pair<Entity, EntityRemoveReason>, Consumer<Entity>> pair = this.pendingEntitiesToRemove.remove();
            Pair<Entity, EntityRemoveReason> entityPair = pair.getFirst();
            Entity entity = entityPair.getFirst();
            EntityRemoveReason reason = entityPair.getSecond();
            forceRemoveEntity(entity, reason);
            Consumer<Entity> listener = pair.getSecond();
            if (listener != null) {
                listener.accept(entity);
            }
        }
    }

    /**
     * Adds the given entity to the chunks entity list without synchronization! <br>
     * NOTE: MINECRAFT THREAD ONLY
     *
     * @param entity the entity to add
     */
    public void forceAddEntity(@NotNull Entity entity) {
        entity.setWorld(world);
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

            if (entity.getNewChunk() == this)
                entity.setChangingToChunk(null); // chunk transfer complete

            // entity update
            if (entity.getNewChunk() == null) {
                long ticksToPerform = entity.getTickJoinMark() == -1 ? 1 : (nTicksPerformed - entity.getTickJoinMark() + 1);
                for (long j = 0; j < ticksToPerform; j++) {
                    long currentTick = mc.getTimer().performedTicks;
                    if (entity.lastUpdated != currentTick) {
                        entity.tick();
                        entity.lastUpdated = currentTick;
                    }
                }
                entity.joinTickAt(-1);
            }

            if (entity.isInChunk(this)) {
                //Entity is still in the same chunk.
                if (entity.isDead()) {
                    entity.onDeath();
                    scheduleRemoveEntity(entity, EntityRemoveReason.ENTITY_DEATH);
                }
            } else if (entity.getNewChunk() == null) {
                //Entity has moved to another chunk
                ChunkBase newChunk = world.getChunkFor(entity);

                //Keeping the entity in the old chunk, if the chunk is indeed null.
                if (newChunk == this) {
                    System.err.println("New chunk for entity is equal to it's current one. world.getChunkFor(entity) returned the wrong chunk, which could either mean rare floating point rounding errors or a bug in the method.");
                    continue;
                }
                if (newChunk != null) {
                    transferEntity(entity, newChunk);
//                    entity.joinTickAt(nTicksPerformed); // start ticking again >>after<< the target chunk performed >>this<< tick, so start ticking next tick
                }
            }
        }
    }

    /**
     * Transfers the entity to a new chunk
     *
     * @param entity   the entity to be transferred
     * @param newChunk the chunk the entity should be transferred into
     */
    protected void transferEntity(@NotNull Entity entity, @NotNull ChunkBase newChunk) {
//        entity.skipUpdateTicks += 1;
        entity.setChangingToChunk(newChunk);
        newChunk.scheduleAddEntity(entity, e -> {
            scheduleRemoveEntity(entity, EntityRemoveReason.ENTITY_CHUNK_CHANGED);
            entity.onChunkChanged(newChunk, this);
        });
        System.out.println("Chunk changed: " + entity.getPositionVector() + " oldChunk: " + this.getChunkOrigin() + ", newChunk: " + newChunk.getChunkOrigin());
    }

    /**
     * Schedules the entity to be removed on the next tick by the chunk.
     *
     * @param entity       the entity to remove
     * @param removeReason the reason why it got removed from the chunk
     */
    @ThreadSafe
    public void scheduleRemoveEntity(@NotNull Entity entity, @NotNull EntityRemoveReason removeReason) {
        this.pendingEntitiesToRemove.add(new Pair<>(new Pair<>(entity, removeReason), null));
    }

    /**
     * Schedules the entity to be removed on the next tick performed by the chunk.
     *
     * @param entity       the entity to remove
     * @param removeReason the reason why it got removed from the chunk
     * @param listener     the listener invoked when the removal is performed. Invoked on a random thread. Only assert the chunk that the entity is in is ticked on this thread that the listener is invoked with
     */
    @ThreadSafe
    public void scheduleRemoveEntity(@NotNull Entity entity, @NotNull EntityRemoveReason removeReason, @NotNull Consumer<Entity> listener) {
        this.pendingEntitiesToRemove.add(new Pair<>(new Pair<>(entity, removeReason), listener));
    }

    /**
     * The method that removes a given entity
     *
     * @param entityIndex  the index of the entity to be removed in the {@link #entities} list
     * @param removeReason the reason the entity was removed from the list
     */
    protected void forceRemoveEntity(int entityIndex, @NotNull EntityRemoveReason removeReason) {
        this.entities.remove(entityIndex);
    }

    /**
     * The method that removes a given entity
     *
     * @param entity       the entity to be removed in the {@link #entities} list
     * @param removeReason the reason the entity was removed from the list
     */
    protected void forceRemoveEntity(@NotNull Entity entity, @NotNull EntityRemoveReason removeReason) {
        this.entities.remove(entity);
    }

    /**
     * @param axisAlignedBB the boundingBox
     * @return if an entity is in the given bounding box
     */
    public boolean isFree(AxisAlignedBB axisAlignedBB) {
        List<Entity> entities = this.entities;
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            if (entity.getBoundingBox().intersects(axisAlignedBB)) {
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
     * @param x block x position
     * @param z block z position
     * @return the state if the given block coordinate is managed by the chunk instance.
     */
    public boolean contains(float x, float z) {
        return x >= this.x && z >= this.z && x < this.x + CHUNK_SIZE && z < this.z + CHUNK_SIZE;
    }

    /**
     * Loads the chunk. The chunk is now being rendered and updated on tick.
     */
    // TODO: DOESN'T SEEM THAT THREAD-SAFE TO MEE... INVESTIGATE FURTHER
    public void load() {
        if (loadedState != 1) {
            world.getWorldChunkHandler().loadChunk(this);
            setLoaded(true);
        }
    }

    /**
     * Unloads the chunks. The chunk is no longer being rendered and updated on tick.
     * The chunk will stay loaded, if the chunk resists to be unloaded on request.
     * Also tells the block state cache to optimize the chunks data.
     * All this happens asynchronous. Invocation of this method will be quick.
     */
    public void unload() {
        if (loadedState != 0) {
            world.getWorldChunkHandler().unloadChunk(this);
            setLoaded(false);
        }
    }

    /**
     * Sets the block state for the given world absolute coordinate.
     *
     * @param x             world x position
     * @param y             world y position
     * @param z             world z position
     * @param newBlockState the new block state
     * @throws IllegalStateException if the specified coordinates are not managed by the chunk.
     */
    public void setBlock(int x, int y, int z, @Nullable IBlockState newBlockState) {
        changeBlock(x, y, z, newBlockState);
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
    protected void changeBlock(int x, int y, int z, @Nullable IBlockState blockState) {
        if (y < 0 || y >= height) {
            throw new IllegalStateException("Chunk Y-index out of bounds: " + y + " for height " + height);
        }
        int relX = x - this.x;
        int relZ = z - this.z;
        if (!this.contains(x, z))
            throw new IllegalStateException("Couldn't set block state to blockState: " + blockState + ". Coordinates x: " + x + ", y: " + y + ", z: " + z + "; relX: " + relX + ", relY: " + y + ", relZ: " + relZ + "; are not contained in ChunkBase: [" + this.toString() + "]");
        ChunkSection section = this.getChunkSection(y);
        section.setBlockState(relX, y - section.getStartHeight(), relZ, blockState);
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
        if (y < 0 || y >= this.height)
            return null; //Returning air, if the block pos is below the world
        int relX = x - this.x;
        int relZ = z - this.z;
        if (!this.contains(x, z))
            throw new IllegalArgumentException("Couldn't get block state of coordinates x: " + x + ", y: " + y + ", z:" + z + "; relX: " + relX + ", relY: " + y + ", relZ: " + relZ + "; are not contained in ChunkBase: [" + this.toString() + "]");
        ChunkSection section = this.getChunkSection(y);
        return section.getRelativeBlockState(relX, y - section.getStartHeight(), relZ);
    }


    /**
     * @param x chunk relative x coordinate
     * @param y chunk relative y coordinate
     * @param z chunk relative  z coordinate
     * @return the block state in the chunk for the specified chunk relative coordinates
     */
    @Nullable
    public IBlockState getRelativeBlockState(int x, int y, int z) {
        if (y < 0 || y >= height)
            return null; //Returning air, if the block pos is below the world
        if (x < 0 || x > CHUNK_SIZE)
            throw new IllegalArgumentException("X-Coordinate " + x + " not in relative chunk bounds.");
        if (z < 0 || z > CHUNK_SIZE)
            throw new IllegalArgumentException("Z-Coordinate " + z + " not in relative chunk bounds.");
        ChunkSection section = this.getChunkSection(y);
        return section.getRelativeBlockState(x, y - section.getStartHeight(), z);
    }

    /**
     * Sets the chunks data accordingly to the information encoded in the bytes
     *
     * @param bytes             the data is the encoded chunk data for this chunk
     * @param chunkSectionsSent the states if the individual chunk sections have been sent in the packet
     */
    //TODO: DOESN'T SEEM THAT THREAD-SAFE... INVESTIGATE FURTHER
    public void setChunkData(@NotNull byte[] bytes, @NotNull boolean[] chunkSectionsSent) {
        this.clearBlocks();
//        BitByteBuffer buf = new BitByteBuffer(bytes, Integer.MAX_VALUE);
        try {
            apply(bytes, chunkSectionsSent);
        } catch (Exception e) {
            mc.getLogger().crash("Cannot deserialize chunk data from byte array!", e);
        }
    }

    /**
     * Applies the chunk state of the specified palette backing array
     */
    private void apply(@NotNull byte[] chunkData, @NotNull boolean[] chunkSectionsSent) {
        int i = 0;
        for (boolean sent : chunkSectionsSent) {
            if (sent) {
                ChunkSection section = chunkSections.get(i);
                chunkData = section.apply(chunkData);
            }
            i++;
        }
    }

    /**
     * Sets all blocks of the chunk to air
     */
    private void clearBlocks() {
        for (ChunkSection chunkSection : this.chunkSections) {
            chunkSection.clearBlocks();
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
    public WorldBase getWorld() {
        return world;
    }

    @NotNull
    public List<Entity> getEntities() {
        return entities;
    }

    public boolean isLoaded() {
        return loadedState == 1;
    }

    public int getLoadedState() {
        return loadedState;
    }

    @Override
    public String toString() {
        return "{loaded: " + this.loadedState + ", entities: " + this.entities.size() + ", x: " + this.x + ", z: " + this.z + ", height: " + this.height + ", id: " + id + "}";
    }

    /**
     * @return a new 2d Vector storing the values of {@link #x} and {@link #z}
     * Note that changes made to this instance do not affect the chunk in any way.
     */
    @NotNull
    public Vec2i getChunkOrigin() {
        return new Vec2i(x, z);
    }

    /**
     * Adds the given entity to the chunk's entity list
     *
     * @param entity the entity to be added
     */
    @ThreadSafe
    public void scheduleAddEntity(@NotNull Entity entity) {
        this.pendingEntitiesToAdd.add(new Pair<>(entity, null));
    }

    /**
     * Adds the given entity to the chunk's entity list
     *
     * @param entity   the entity to be added
     * @param listener the listener invoked when the add operation is performed. Invoked on a random thread. Only assert the chunk that the entity is in is ticked on this thread that the listener is invoked with
     */
    @ThreadSafe
    public void scheduleAddEntity(@NotNull Entity entity, @NotNull Consumer<Entity> listener) {
        this.pendingEntitiesToAdd.add(new Pair<>(entity, listener));
    }

    /**
     * @param region    the region
     * @param predicate the condition the entity must fulfill
     * @return a collection of entities that are in the given region and fulfill the requirements of the predicate.
     */
    @NotNull
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
     * Sets {@link #loadedState} to state
     *
     * @param state the new state of loaded
     */
    @Unsafe
    public void setLoaded(boolean state) {
        this.loadedState = state ? 1 : 0;
    }

    /**
     * @return the chunks bounding-box
     */
    @NotNull
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    @NotNull
    public List<ChunkSection> getChunkSections() {
        return chunkSections;
    }

    /**
     * @param y the y position
     * @return the chunk section at the given y position
     */
    @NotNull
    protected ChunkSection getChunkSection(int y) {
        y /= CHUNK_SECTION_SIZE;
        if (y < 0 || y >= this.chunkSections.size())
            throw new IllegalArgumentException("Could not retrieve ChunkSection for y position out of chunk bounds: " + y);
        return this.chunkSections.get(y);
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
