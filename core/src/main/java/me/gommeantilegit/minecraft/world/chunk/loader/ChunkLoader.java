package me.gommeantilegit.minecraft.world.chunk.loader;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.util.collections.ThreadBoundList;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import me.gommeantilegit.minecraft.world.chunk.builder.ChunkRebuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.lang.StrictMath.hypot;
import static me.gommeantilegit.minecraft.world.chunk.Chunk.CHUNK_SIZE;

public class ChunkLoader implements AsyncOperation, Tickable {

    /**
     * Parent world object
     */
    @NotNull
    private final World world;

    /**
     * State of chunks needing to be unloaded
     */
    private boolean chunksToUnload;

    /**
     * State of chunks needing to be loaded
     */
    private boolean chunksToLoad;

    /**
     * Maximum distance to a chunk that can be loaded
     */
    private int renderDistance;

    /**
     * Sorter object for sorting chunks by their distance.
     *
     * @see DistanceSorter
     */
    private final DistanceSorter distanceSorter;

    /**
     * Object for rebuilding meshes of chunks
     */
    @NotNull
    public final ChunkRebuilder chunkRebuilder;

    /**
     * Queue of chunks that need to be unloaded
     */
    @NotNull
    private final Queue<Chunk> unloadQueue = new LinkedList<>();

    /**
     * Queue of chunks that need to be loaded
     */
    @NotNull
    private final Queue<Chunk> loadQueue = new LinkedList<>();

    /**
     * @param world sets {@link #world}
     */
    public ChunkLoader(@NotNull World world) {
        this.world = world;
        this.renderDistance = world.worldRenderer.getRenderDistance();
        this.chunkRebuilder = new ChunkRebuilder(world);
        this.distanceSorter = new DistanceSorter(world.viewer, world);
    }

    @Override
    public void tick(float partialTicks) {
        this.unloadChunks();
        this.loadChunks();
    }

    /**
     * Loads all chunks that are queued in {@link #loadQueue}
     */
    private void loadChunks() {
        if (this.chunksToLoad) {
            while (!this.loadQueue.isEmpty()) {
                try {
                    Chunk chunk = this.loadQueue.remove();
                    //Loading the chunk queued in the loadQueue
                    {
                        if (!chunk.isLoaded()) {
                            this.world.getWorldChunkHandler().addLoadedChunk(chunk);
                            this.world.getWorldChunkHandler().removeUnloadedChunk(chunk);
                            chunk.setLoaded(true);
                            if (chunk.getMesh() == null)
                                chunk.setNeedsRebuild(true);
                        }
                    }
                } catch (NoSuchElementException ignored) {

                }
            }
            this.chunksToLoad = false;
        }
    }

    /**
     * Unloads all chunks that are queued in {@link #unloadQueue}
     */
    private void unloadChunks() {
        if (chunksToUnload) {
            try {
                while (!this.unloadQueue.isEmpty()) {
                    Chunk chunk = this.unloadQueue.remove();
                    //Unloading the chunk of the queue
                    {
                        boolean canUnload = chunk.checkForUnload();
                        if (!canUnload) return;
                        if (chunk.isLoaded()) {
                            chunk.setLoaded(false);
                            this.world.getWorldChunkHandler().removeLoadedChunk(chunk);
                            this.world.getWorldChunkHandler().addUnloadedChunk(chunk);
                        }  //else throw new IllegalStateException("Cannot scheduleUnload unloaded chunk: " + this.toString());
                    }
                }
            } catch (NoSuchElementException ignored) {
            }
            this.chunksToUnload = false;
        }
    }

    /**
     * Updates the chunkLoader. Loads / unloads chunk during this call, if necessary
     **/
    @Override
    public void onAsyncThread() {

        this.sortChunksByDistance();
        {
            Minecraft.mc.tickProfiler.actionStart("Chunk-Loading");
            Vector2 playerPosition; //2D vector storing the viewers x and y coordinates
            {
                Vector3 viewingPosition = world.viewer.getPositionVector();
                playerPosition = new Vector2(viewingPosition.x, viewingPosition.z);
            }
            ThreadBoundList<Chunk> chunks = this.world.getWorldChunkHandler().getChunks();
            for (int i = 0; i < chunks.size(); i++) {
                Chunk chunk = chunks.get(i);
                double distance;
                {
                    Vector2 chunkPosition = chunk.getChunkOrigin();
                    distance = playerPosition.dst(chunkPosition);
                }
                // Checking if the chunk is in render distance
                if (distance < renderDistance) {
                    if (!chunk.isLoaded()) {
                        chunk.load();
                    }
                } else {
                    if (chunk.isLoaded()) {
                        scheduleUnload(chunk);
                    }
                }
            }
            Minecraft.mc.tickProfiler.actionEnd("Chunk-Loading");
        }
        this.chunkRebuilder.onAsyncThread();
    }

    /**
     * @param renderDistance maximum distance to a chunk that can be loaded
     */
    public void setRenderDistance(int renderDistance) {
        this.renderDistance = renderDistance;
    }

    /**
     * Schedules the Unload of the given chunk
     *
     * @param chunk the given chunk
     */
    public void scheduleUnload(Chunk chunk) {
        this.unloadQueue.add(chunk);
        this.chunksToUnload = true;
    }

    /**
     * Schedules the chunk's load
     *
     * @param chunk the given chunk
     */
    public void scheduleLoad(Chunk chunk) {
        this.loadQueue.add(chunk);
        this.chunksToLoad = true;
    }

    /**
     * Sorter for sorting chunks by distance relative to a specified player as a viewer
     */
    private static final class DistanceSorter implements Comparator<Chunk> {

        /**
         * The player viewing the chunks
         */
        @NotNull
        private final Player viewer;


        private final ArrayList<Chunk> sortedChunks = new ArrayList<>();

        /**
         * The world whose chunks should be sorted.
         */
        @NotNull
        private final World world;

        /**
         * @param viewer sets {@link #viewer}
         */
        private DistanceSorter(@NotNull Player viewer, @NotNull World world) {
            this.viewer = viewer;
            this.world = world;
        }

        @Override
        public int compare(@NotNull Chunk chunk1, @NotNull Chunk chunk2) {
            double dif = distance(chunk1) - distance(chunk2);
            return dif > 0 ? 1 : dif < 0 ? -1 : 0;
        }

        /**
         * @param chunk the specified chunk that the distance should be measured to
         * @return the distance of the chunk to the player ({@link #viewer})
         */
        private double distance(@NotNull Chunk chunk) {
            return hypot((chunk.getX() + CHUNK_SIZE / 2f) - viewer.posX, (chunk.getZ() + CHUNK_SIZE / 2f) - viewer.posZ);
        }

        /**
         * The position vector of the player, when the {@link #world}'s chunks were last sorted.
         */
        @Nullable
        private Vector3 lastSorted = null;

        private int lastChunksSize;

        /**
         * Sorts the chunks of the world, if necessary
         */
        void sortChunks() {
            //EMPTY FOR NOW
//            synchronized (world.getChunks()) {
//                for (Chunk chunk : this.world.getChunks()) {
//                    if (!this.sortedChunks.contains(chunk)) {
//                        this.sortedChunks.add(chunk);
//                    }
//                }
//                ArrayList<Chunk> chunks = sortedChunks;
//                if (lastSorted == null || (lastSorted.dst(viewer.getPositionVector()) > this.world.worldRenderer.getRenderDistance() / 8) || chunks.size() != lastChunksSize) {
//                    sort(chunks, this);
//                    lastSorted = viewer.getPositionVector();
//                }
//                lastChunksSize = chunks.size();
//            }
        }
    }

    /**
     * Sorts all chunks according to their distance to {@link Minecraft#thePlayer}, if sorting is necessary
     *
     * @see DistanceSorter
     */
    private void sortChunksByDistance() {
        distanceSorter.sortChunks();
    }
}
