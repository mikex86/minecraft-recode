package me.gommeantilegit.minecraft.world.chunk.creator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.profiler.Profiler;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import me.gommeantilegit.minecraft.world.chunk.world.WorldChunkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Object generating chunks around entities and handling the access to the chunk array lists
 */
public class ChunkCreator implements Tickable, AsyncOperation {

    /**
     * The parent world
     */
    @NotNull
    private final World world;

    /**
     * Player viewing the world
     */
    @NotNull
    private final Player viewer;

    /**
     * Stores all entities that still need to be assigned to chunks
     */
    @NotNull
    private final Queue<Entity> pendingEntities = new LinkedList<>();

    /**
     * Stores all Chunks that need to be added to the given Chunk lists of the World instances {@link WorldChunkHandler}
     */
    @NotNull
    private final Queue<Vector2> addQueue = new LinkedList<>();

    /**
     * State whether currently chunks are scheduled to be added.
     */
    private boolean chunksScheduled = false;

    /**
     * State whether currently entities are scheduled to be added.
     */
    private boolean entitiesPending = false;

    public ChunkCreator(@NotNull World world) {
        this.world = world;
        this.viewer = world.viewer;
    }

    /**
     * Profiler for {@link #tick(float)}
     */
    @NotNull
    public final Profiler chunkCreatorProfiler = new Profiler("ChunkCreatorProfiler", false);

    @Override
    public void tick(float partialTicks) {
        chunkCreatorProfiler.actionStart();
        this.addChunks();
        chunkCreatorProfiler.actionEnd();
    }

    /**
     * Adds all chunks that are queued in {@link #addQueue}
     */
    private void addChunks() {
        if (chunksScheduled) {
            while (!addQueue.isEmpty()) {
                //Adding the chunk
                try {
                    Vector2 chunkOrigin = addQueue.remove();
                    Chunk chunkFor = world.getChunkFor(chunkOrigin.x, chunkOrigin.y);
                    if (chunkFor != null) throw new IllegalStateException("getChunkOrigin(x, z) messed up!");
//                    if (world.getWorldChunkHandler().getChunks().stream().anyMatch(c -> c.contains((int) chunkOrigin.x, (int) chunkOrigin.y)))
//                        throw new IllegalStateException("Everything is messed up...");
                    addChunk(new Chunk(world.height, (int) chunkOrigin.x, (int) chunkOrigin.y, world));
                } catch (NoSuchElementException ignored) {

                }
            }
            chunksScheduled = false;
        }
    }

    @Override
    public void onAsyncThread() {
        updatePendingEntities();
        generateChunksAroundEntity(viewer, world.worldRenderer.getRenderDistance());
    }

    /**
     * Handles all pending entities
     */
    private void updatePendingEntities() {
        if (entitiesPending) {
            while (!pendingEntities.isEmpty()) {
                try {
                    Entity entity = pendingEntities.element();
                    if (handle(entity))
                        pendingEntities.remove();
                } catch (NoSuchElementException ignored) {
                }
            }
            entitiesPending = false;
        }
    }

    /**
     * Creates the needed chunk for the given entity and assigns it to it's chunk.
     *
     * @param entity the given entity
     * @return true, if the entity was scheduled to be added to the created chunk. False, if the entity is still pending, thus waiting for it's chunks to be created asynchronously.
     */
    private boolean handle(@NotNull Entity entity) {
        generateChunksAroundEntity(entity, Chunk.CHUNK_SIZE * 2);
        Chunk chunk = world.getChunkFor(entity);
        if (chunk != null) {
            chunk.scheduleAddEntity(entity);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Generates chunks around the entity's position.
     *
     * @param entity      the entity that chunks should be generated around
     * @param maxDistance the maximum distance to a chunk that should be created
     */
    private void generateChunksAroundEntity(@NotNull Entity entity, int maxDistance) {
        for (int zo = -maxDistance / 2; zo < maxDistance / 2; zo++) {
            for (int xo = -maxDistance / 2; xo < maxDistance / 2; xo++) {
                float x = entity.posX + xo, z = entity.posZ + zo;
                Vector2 chunkPosition = new Vector2(x, z);
                double distance;
                {
                    Vector2 entityPosition;
                    {
                        Vector3 entityVector3 = entity.getPositionVector();
                        entityPosition = new Vector2(entityVector3.x, entityVector3.z);
                    }
                    distance = entityPosition.dst(chunkPosition);
                }
                if (distance < maxDistance)
                    tryCreateChunkFor(chunkPosition);
            }
        }
        world.setLoaded(true);
    }

    /**
     * Submits an entity to the chunk creator that handles the creation of the chunks needed for the entity to spawn and the assignation to it's corresponding chunk after it's created.
     *
     * @param entity the given entity
     */
    public void submit(@NotNull Entity entity) {
        this.pendingEntities.add(entity);
        this.entitiesPending = true;
    }

    /**
     * Defines the chunk that contains the given position vector accordingly to the chunk grid of CHUNK_SIZE by CHUNK_SIZE meaning that chunk origins are always multiples of CHUNK_SIZE. (either positive or negative)
     *
     * @param vec the given position vector (2D vector. x represents x axis of world; y represents z axis of world) (Top View)
     * @return true if a chunk has been defined. If false, a chunk already handles the given area that the vector is in.
     */
    public boolean tryCreateChunkFor(@NotNull Vector2 vec) {
        Vector2 origin = world.getChunkOrigin(vec.x, vec.y).asLibGDXVec2D();
        if (!world.getWorldChunkHandler().chunkExistsAt(origin)) {
            createChunk(origin);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the given chunk from the world's chunk list.
     * <br>NOTE: USE MINECRAFT THREAD ONLY<br>
     *
     * @param chunk the given chunk.
     */
    public void removeChunk(@NotNull Chunk chunk) {
        world.getWorldChunkHandler().getChunks().remove(chunk);
    }

    /**
     * Creates the chunk that contains the given position regardless of whether or not a chunk is already defined for the given region. Invoked by {@link #tryCreateChunkFor(Vector2)}
     *
     * @param origin the chunk origin where to create the chunk
     */
    private void createChunk(@NotNull Vector2 origin) {
        world.getWorldChunkHandler().addChunkOrigin(origin);
        scheduleChunkCreation(origin);
    }

    /**
     * Schedules the creation of a chunk for this x, z vector on this {@link ChunkCreator} instance to be performed on the Minecraft Thread
     *
     * @param vec the chunk vector where the chunk should be added
     */
    private void scheduleChunkCreation(Vector2 vec) {
        this.chunksScheduled = true;
        this.addQueue.add(vec);
    }

    /**
     * Adds the given chunk to the world instance.
     * - Invokes the creator listener {@link World#onChunkCreationListener} of {@link #world}
     * - Adds the instance to {@link WorldChunkHandler#chunks} of {@link World#worldChunkHandler} of {@link #world}
     * <p><br>
     * NOTE: The listener is invoked before the chunk is added to the list. (If the listener is present meaning the field is not null)<br>
     * ALSO NOTE: USE MINECRAFT THREAD ONLY<br>
     *
     * @param chunk the chunk to be added
     * @see World.OnChunkCreationListener
     */
    private void addChunk(@NotNull Chunk chunk) {
        if (this.world.getOnChunkCreationListener() != null) {
            this.world.getOnChunkCreationListener().onChunkCreated(chunk);
        }
        this.world.applyBlockChanges(chunk); //Changing up the chunk by applying the block changes regarding this chunk
        this.world.getWorldChunkHandler().getChunks().add(chunk); // Adding the chunk to the world chunks list
        this.world.getWorldChunkHandler().addUnloadedChunk(chunk); //Adding the chunk to the list of unloaded chunks because the chunk is not loaded by default and therefore unloaded. If it gets loaded later on, it gets removed.
    }
}
