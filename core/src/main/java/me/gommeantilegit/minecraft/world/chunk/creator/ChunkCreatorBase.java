package me.gommeantilegit.minecraft.world.chunk.creator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.annotations.Unsafe;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.lang.Math.ceil;

/**
 * Object generating chunks around entities and handling the access to the chunk array lists
 */
public abstract class ChunkCreatorBase<WB extends WorldBase<?, ?, ?, WB, ?, CB, ?, ?, ?>, CB extends ChunkBase<?, CB, ?, ?, ?, ?, ?, WB, ?>> implements AsyncOperation {

    /**
     * The parent world
     */
    @NotNull
    protected final WB world;

    /**
     * Players viewing the world
     */
    @NotNull
    private final List<PlayerBase> viewers;

    /**
     * Stores all entities that still need to be assigned to chunks
     */
    @NotNull
    private final Queue<Entity> pendingEntities = new LinkedList<>();

    /**
     * State whether currently entities are scheduled to be added.
     */
    private boolean entitiesPending = false;

    public ChunkCreatorBase(@NotNull WB world) {
        this.world = world;
        this.viewers = new ArrayList<>();
    }

    /**
     * Adds a chunk to the world
     *
     * @param chunkX chunk X origin
     * @param chunkZ chunk Z origin
     * @param world  the world
     */
    protected abstract void addChunk(int chunkX, int chunkZ, WB world);

    @Override
    public void onAsyncThread() {
        updatePendingEntities();
        for (int i = 0; i < viewers.size(); i++) {
            Entity ent = viewers.get(i);
            generateChunksAroundEntity(ent, getChunkLoadingDistance(ent));
        }
    }

    /**
     * @param ent the given entity
     * @return the chunk loading distance for the given entity
     */
    protected abstract int getChunkLoadingDistance(@NotNull Entity ent);

    /**
     * Handles all pending entities
     */
    protected void updatePendingEntities() {
        if (entitiesPending) {
            synchronized (pendingEntities) {
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
    }

    /**
     * Creates the needed chunk for the given entity and assigns it to it's chunk.
     *
     * @param entity the given entity
     * @return true, if the entity was scheduled to be added to the created chunk. False, if the entity is still pending, thus waiting for it's chunks to be created asynchronously.
     */
    private boolean handle(@NotNull Entity entity) {
        CB chunk = world.getChunkFor(entity);
        if (chunk != null) {
            chunk.addEntity(entity);
            return true;
        } else {
            generateChunksAroundEntity(entity, ChunkBase.CHUNK_SIZE * 2);
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
        generateChunksAroundPosition(entity.getPositionVector(), maxDistance);
    }

    /**
     * Generates chunks around the specified position.
     *
     * @param positionVector the position that chunks should be generated around
     * @param maxDistance    the maximum distance to a chunk that should be created
     */
    public void generateChunksAroundPosition(@NotNull Vector3 positionVector, int maxDistance) {
        int range = (int) ceil(((maxDistance / 2f) / (double) ChunkBase.CHUNK_SIZE)) * ChunkBase.CHUNK_SIZE;
        for (int zo = -range; zo < range; zo += ChunkBase.CHUNK_SIZE) {
            for (int xo = -range; xo < range; xo += ChunkBase.CHUNK_SIZE) {
                float x = positionVector.x + xo, z = positionVector.z + zo;

                Vector2 origin = new Vector2(x, z);
                double distance;
                {
                    Vector2 pos;
                    {
                        pos = new Vector2(positionVector.x, positionVector.z);
                    }
                    distance = pos.dst(origin);
                }
                if (distance < maxDistance)
                    tryCreateChunkFor(new Vec2i(origin));
            }
        }
        if (!world.isLoaded)
            world.isLoaded = true;
    }

    /**
     * Submits an entity to the chunk creator that handles the creation of the chunks needed for the entity to spawn and the assignation to it's corresponding chunk after it's created.
     *
     * @param entity the given entity
     */
    public void submit(@NotNull Entity entity) {
        synchronized (pendingEntities) {
            this.pendingEntities.add(entity);
            this.entitiesPending = true;
        }
    }

    /**
     * Defines the chunk that contains the given position vector accordingly to the chunk grid of CHUNK_SIZE by CHUNK_SIZE meaning that chunk origins are always multiples of CHUNK_SIZE. (either positive or negative)
     *
     * @param position the given position vector (2D vector. x represents x axis of world; y represents z axis of world) (Top View)
     * @return true if a chunk has been defined. If false, a chunk already handles the given area that the vector is in.
     */
    public boolean tryCreateChunkFor(@NotNull Vec2i position) {
        Vec2i origin = world.getChunkOrigin(position.getX(), position.getY());
        if (!world.getWorldChunkHandler().chunkExistsAtOrigin(origin)) {
            createChunk(origin);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates the chunk that contains the given position regardless of whether or not a chunk is already defined for the given region. Invoked by {@link #tryCreateChunkFor(Vec2i)}
     *
     * @param origin the chunk origin where to create the chunk
     */
    private synchronized void createChunk(@NotNull Vec2i origin) {
        world.getWorldChunkHandler().addChunkOrigin(origin);
        synchronized (world.getWorldChunkHandler().getLoadedChunks()) {
            addChunk(origin.getX(), origin.getY(), world);
        }
    }

    /**
     * Adds the given chunk to the world instance
     *
     * @param chunk the chunk to be added
     */
    @Unsafe
    protected void addChunk(@NotNull CB chunk) {
        this.world.getWorldChunkHandler().addChunk(chunk); // Adding the chunk to the world chunks list
        this.world.getWorldChunkHandler().addUnloadedChunk(chunk); // Adding the chunk to the list of unloaded chunks because the chunk is not loaded by default and therefore unloaded. If it gets loaded later on, it gets removed.
    }

    /**
     * Adds a new viewer to the list of viewers
     *
     * @param viewer the viewer
     */
    public void addViewer(@NotNull PlayerBase viewer) {
        this.viewers.add(viewer);
    }

    public List<PlayerBase> getViewers() {
        return viewers;
    }
}
