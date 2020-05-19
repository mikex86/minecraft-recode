package me.gommeantilegit.minecraft.world.chunk.loader;

import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.util.VectorUtils;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.world.WorldChunkHandlerBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ChunkLoaderBase implements Tickable {

    /**
     * Parent world object
     */
    @NotNull
    protected final WorldBase world;

    /**
     * Stores the list of chunks loaded for a given entity
     */
    @NotNull
    private final Map<Entity, List<ChunkBase>> entityParentChunkLoads = new ConcurrentHashMap<>();

    /**
     * Players viewing the world
     */
    @NotNull
    @ThreadSafe
    private final List<PlayerBase> viewers;

    /**
     * Executes the actions asynchronously
     */
    @NotNull
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), r -> {
        Thread thread = new Thread(r, "ChunkLoader-Worker");
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        return thread;
    });

    /**
     * @param world sets {@link #world}
     */
    public ChunkLoaderBase(@NotNull WorldBase world) {
        this.world = world;
        this.viewers = new CopyOnWriteArrayList<>();
    }

    /**
     * @param ent the given entity
     * @return the chunk loading distance for the given entity
     */
    public abstract int getChunkLoadingDistance(@NotNull Entity ent);

    /**
     * @return true if the chunk is in chunk loading distance of the given entity
     */
    private boolean isInDistance(@NotNull ChunkBase chunk, @NotNull Entity entity) {
        int chunkLoadingDistance = getChunkLoadingDistance(entity);
        return chunk.getChunkOrigin().getDistance(VectorUtils.xzTo2Di(entity.getPositionVector())) < chunkLoadingDistance;
    }

    /**
     * Keeps track of entity chunk changes and loads the appropriate chunks
     *
     * @param entity   the entity that changed chunk
     * @param newChunk the chunk it changed into
     * @param oldChunk the old chunk it came from, or null
     */
    public void trackEntityChunkChange(@NotNull Entity entity, @NotNull ChunkBase newChunk, @Nullable ChunkBase oldChunk) {
        if (!(entity instanceof PlayerBase)) {
            return;
        }
        if (!this.viewers.contains(entity)) {
            return;
        }
        scheduleChunkActions(entity);
    }


    /**
     * Keeps track of spawning entities by loading the appropriate chunks
     *
     * @param chunk the chunk
     */
    public void trackEntitySpawn(@NotNull Entity entity, @NotNull ChunkBase chunk) {
        if (!(entity instanceof PlayerBase)) {
            return;
        }
        if (!this.viewers.contains(entity)) {
            return;
        }
        if (!chunk.isLoaded())
            load(chunk, entity);

        scheduleChunkActions(entity);
    }

    protected void scheduleChunkActions(@NotNull Entity entity) {
        this.executorService.submit(() -> performChunkActions(entity));
    }

    protected void performChunkActions(@NotNull Entity entity) {
        long start = System.currentTimeMillis();
        int chunkLoadingDistance = getChunkLoadingDistance(entity);
        this.world.getChunkCreator().generateChunksAroundEntity(entity, chunkLoadingDistance);
        List<ChunkBase> loadedForEntity = getChunksLoadedFor(entity);

        // unload chunks
        List<ChunkBase> toUnload = getUnloadableChunks(entity, loadedForEntity, chunk -> !isInDistance(chunk, entity));
        for (ChunkBase chunk : toUnload) {
            unload(chunk, entity);
        }

        // load chunks
        List<ChunkBase> toLoad = getLoadableChunks(chunk -> isInDistance(chunk, entity));
        for (ChunkBase chunk : toLoad) {
            load(chunk, entity);
        }
        long end = System.currentTimeMillis();
//            System.out.println("Chunk loading took: " + (end - start) + " ms");
    }

    protected synchronized void load(@NotNull ChunkBase chunk, @NotNull Entity entity) {
        if (chunk.isLoaded())
            return;
        List<ChunkBase> entityChunks = getChunksLoadedFor(entity);
        entityChunks.add(chunk);
        chunk.load();
//        System.out.println("load");
    }

    protected synchronized void unload(@NotNull ChunkBase chunk, @NotNull Entity entity) {
        if (!chunk.isLoaded())
            throw new IllegalStateException("Tried to unload an unloaded or uninitialized chunk: " + chunk);
        List<ChunkBase> entityChunks = getChunksLoadedFor(entity);
        entityChunks.remove(chunk);
        chunk.unload();
//        System.out.println("unload: " + chunk);
    }

    /**
     * @param predicate the chunk predicate
     * @return all loadable (state != loaded) chunks that match the given predicate
     */
    @NotNull
    private List<ChunkBase> getLoadableChunks(@NotNull Predicate<ChunkBase> predicate) {
        WorldChunkHandlerBase chunkHandler = this.world.getWorldChunkHandler();
        Stream<ChunkBase> chunks = chunkHandler.getLoadableChunks()
                .map(this::reportDeleted)
                .filter(Objects::nonNull)
                .filter(predicate);
        return chunks.collect(Collectors.toList());
    }

    @Nullable
    private ChunkBase reportDeleted(@NotNull Reference<ChunkBase> reference) {
        WorldChunkHandlerBase chunkHandler = this.world.getWorldChunkHandler();
        ChunkBase chunk = reference.get();
        if (chunk == null) {
            // TODO: report deleted here
            return null;
        }
        return chunk;
    }

    /**
     * Unloads the chunks of #loadedChunks that match #predicate and are not contained in any other viewers loaded chunk list
     */
    @NotNull
    private List<ChunkBase> getUnloadableChunks(@NotNull Entity viewer, @NotNull List<ChunkBase> loadedChunks, @NotNull Predicate<ChunkBase> predicate) {
        List<ChunkBase> unloadable = new ArrayList<>();
        for (ChunkBase chunk : loadedChunks) {
            if (predicate.test(chunk)) {
                unloadable.add(chunk);
            }
        }
        List<Entity> otherViewers = getOtherViewers(viewer);
        List<ChunkBase> otherLoadedChunks = getLoadedChunks(otherViewers); // all chunks loaded by other entities
        unloadable.removeAll(otherLoadedChunks);
        return unloadable;
    }

    @NotNull
    private List<Entity> getOtherViewers(@NotNull Entity viewer) {
        List<Entity> otherViewers = new ArrayList<>(this.viewers);
        otherViewers.remove(viewer);
        return otherViewers;
    }

    /**
     * @param entities the list of entities
     * @return all chunks loaded by all the entities supplied
     */
    @NotNull
    protected List<ChunkBase> getLoadedChunks(@NotNull List<Entity> entities) {
        List<ChunkBase> chunkList = new ArrayList<>();
        for (Entity entity : entities) {
            List<ChunkBase> entityChunks = getChunksLoadedFor(entity);
            chunkList.addAll(entityChunks);
        }
        return chunkList;
    }

    @NotNull
    protected List<ChunkBase> getChunksLoadedFor(@NotNull Entity entity) {
        List<ChunkBase> chunks = this.entityParentChunkLoads.get(entity);
        if (chunks == null)
            throw new IllegalStateException("Could not retrieve chunks loaded for entity which is not a viewer: " + entity);
        return chunks;
    }

    /**
     * Adds a new viewer to the list of viewers
     *
     * @param viewer the viewer
     */
    @ThreadSafe
    public void addViewer(@NotNull PlayerBase viewer) {
        this.viewers.add(viewer);
        this.entityParentChunkLoads.put(viewer, new CopyOnWriteArrayList<>());
    }

    /**
     * Removes a viewer from the list of viewers
     *
     * @param viewer the viewer
     */
    @ThreadSafe
    public void removeViewer(@NotNull PlayerBase viewer) {
        List<ChunkBase> chunks = this.entityParentChunkLoads.get(viewer);
        List<ChunkBase> toUnload = getUnloadableChunks(viewer, chunks, chunk -> true);
        for (ChunkBase chunk : toUnload) {
            unload(chunk, viewer);
        }
        this.viewers.remove(viewer);
    }

    public void trackChunkLoadingDistanceChange(int renderDistance) {
        for (PlayerBase viewer : viewers) {
            scheduleChunkActions(viewer);
        }
    }
}
