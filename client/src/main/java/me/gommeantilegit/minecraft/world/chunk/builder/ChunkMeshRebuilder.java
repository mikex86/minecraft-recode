package me.gommeantilegit.minecraft.world.chunk.builder;

import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.block.ClientBlockRendererTypeRegistry;
import me.gommeantilegit.minecraft.block.access.IReadableBlockStateAccess;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.block.state.storage.BlockStateStorage;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerSP;
import me.gommeantilegit.minecraft.rendering.GLContext;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec3i;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkSection;
import me.gommeantilegit.minecraft.world.chunk.ClientChunkSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@SideOnly(side = Side.CLIENT)
public class ChunkMeshRebuilder implements AsyncOperation {

    /**
     * Stores the registry for block renderers to use for chunk mesh building
     */
    @NotNull
    private final ClientBlockRendererTypeRegistry rendererRegistry;

    /**
     * Executor Service to rebuild chunk meshes
     */
    @NotNull
    private final ExecutorService executorService = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()), r -> {
        Thread thread = new Thread(r, "ChunkRebuilder-PoolThread");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });

//    /**
//     * Mutex for the chunk mesh re-builder state
//     */
//    @NotNull
//    private final Object mutex = new Object();

    @NotNull
    private final Map<ClientChunkSection, CompletableFuture<OptimizedMeshBuilder>> futures = new ConcurrentHashMap<>();

    @NotNull
    private final EntityPlayerSP viewer;

    public ChunkMeshRebuilder(@NotNull EntityPlayerSP viewer, @NotNull ClientBlockRendererTypeRegistry rendererRegistry) {
        this.rendererRegistry = rendererRegistry;
        this.viewer = viewer;
    }

    /**
     * Schedules the given chunk to have it's mesh rebuilt
     *
     * @param section the chunk that needs it's mesh to be rebuilt
     */
    @ThreadSafe
    public void scheduleSectionMeshRebuild(@NotNull ClientChunkSection section) {
//        CompletableFuture.runAsync(() -> {
//            synchronized (this.mutex) {
        this.cancelRebuildTask(section);
        CopyingNeighboringSectionStateProvider blockStateAccess = new CopyingNeighboringSectionStateProvider(section);

        Vec3i startPos = new Vec3i(0, 0, 0);
        Vec3i endPos = new Vec3i(ChunkBase.CHUNK_SIZE, ChunkSection.CHUNK_SECTION_SIZE, ChunkBase.CHUNK_SIZE);

        ChunkMeshingTask task = new ChunkMeshingTask(blockStateAccess, startPos, endPos, this.rendererRegistry);
        this.runMeshingTask(section, task)
                .thenAcceptAsync(meshBuilder -> this.finishMesh(meshBuilder, section))
                .exceptionally(e -> {
                    this.onException(e);
                    return null;
                })
                .whenCompleteAsync((aVoid, throwable) -> {
                    blockStateAccess.delete();
//                            synchronized (this.mutex) {
                    this.futures.remove(section);
//                            }
                })
                .exceptionally(e -> {
                    e.printStackTrace(); // Fatal exception from whenComplete block
                    return null;
                });
//            }
//        });
    }

    private void onException(@NotNull Throwable throwable) {
        if (throwable.getCause() instanceof CancellationException)
            return;

        throwable.printStackTrace();
    }

    private void finishMesh(@Nullable OptimizedMeshBuilder meshBuilder, @NotNull ClientChunkSection section) {
        if (meshBuilder == null) {
            return;
        }
        GLContext.getGlContext().runOnGLContext(() -> {
            section.setMesh(meshBuilder.end());
            section.onMeshBuildComplete();
        });
    }

    @NotNull
    private CompletionStage<OptimizedMeshBuilder> runMeshingTask(@NotNull ClientChunkSection parentSection, @NotNull ChunkMeshingTask task) {
//        synchronized (this.mutex) {
        CompletableFuture<OptimizedMeshBuilder> future = CompletableFuture.supplyAsync(task, this.executorService);
        if (this.futures.put(parentSection, future) != null) {
            throw new IllegalStateException();
        }
        return future;
//    }
    }

    @ThreadSafe
    public void cancelRebuildTask(@NotNull ClientChunkSection section) {
//        synchronized (this.mutex) {
        CompletableFuture<OptimizedMeshBuilder> future = this.futures.remove(section);
        if (future != null) {
            // just cancel if it's still running
            future.cancel(true);
            section.onMeshBuildCancelled();
        }
//        }
    }

    @Override
    public void stopAsyncWork() {
        this.executorService.shutdown();
    }

    private static class CopyingNeighboringSectionStateProvider implements IReadableBlockStateAccess {

        @NotNull
        private final List<BlockStateStorage> neighboringStorages;

        @NotNull
        private final BlockStateStorage mainStorage;

        private CopyingNeighboringSectionStateProvider(@NotNull ChunkSection chunkSection) {
            BlockStateStorage mainStorage = chunkSection.getBlockStorageCopy(); // copy block storage
            List<ChunkSection> neighborSections = chunkSection.getNeighboringSections(); // get neighboring sections
            List<BlockStateStorage> storages = new ArrayList<>();
            // copy neighbor sections
            for (ChunkSection neighbor : neighborSections) {
                if (neighbor != null)
                    storages.add(neighbor.getBlockStorageCopy());
                else
                    storages.add(null); // preserve index to position mapping
            }
            this.neighboringStorages = storages;
            this.mainStorage = mainStorage;
        }

        @Nullable
        @Override
        public IBlockState getBlockState(int x, int y, int z) {
            BlockStateStorage neighbor;
            if (x < 0) {
                neighbor = neighboringStorages.get(0);
                return neighbor.getBlockState(x + ChunkBase.CHUNK_SIZE, y, z);
            }
            if (x >= ChunkBase.CHUNK_SIZE) {
                neighbor = neighboringStorages.get(1);
                if (neighbor != null) {
                    return neighbor.getBlockState(x - ChunkBase.CHUNK_SIZE, y, z);
                } else {
                    return null;
                }
            }
            if (z < 0) {
                neighbor = neighboringStorages.get(2);
                if (neighbor != null) {
                    return neighbor.getBlockState(x, y, z + ChunkBase.CHUNK_SIZE);
                } else {
                    return null;
                }
            }
            if (z >= ChunkBase.CHUNK_SIZE) {
                neighbor = neighboringStorages.get(3);
                if (neighbor != null) {
                    return neighbor.getBlockState(x, y, z - ChunkBase.CHUNK_SIZE);
                } else {
                    return null;
                }
            }
            if (y < 0) {
                neighbor = neighboringStorages.get(4);
                if (neighbor != null) {
                    return neighbor.getBlockState(x, y + ChunkSection.CHUNK_SECTION_SIZE, z);
                } else {
                    return null;
                }
            }
            if (y >= ChunkSection.CHUNK_SECTION_SIZE) {
                neighbor = neighboringStorages.get(5);
                if (neighbor != null) {
                    return neighbor.getBlockState(x, y - ChunkSection.CHUNK_SECTION_SIZE, z);
                } else {
                    return null;
                }
            }
            return mainStorage.getBlockState(x, y, z);
        }

        public void delete() {
            this.mainStorage.delete();
            for (BlockStateStorage storage : this.neighboringStorages) {
                if (storage == null) continue;
                storage.delete();
            }
        }
    }
}
