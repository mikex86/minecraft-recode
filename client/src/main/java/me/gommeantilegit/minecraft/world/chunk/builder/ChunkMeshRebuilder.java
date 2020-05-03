package me.gommeantilegit.minecraft.world.chunk.builder;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import kotlin.Pair;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.ClientBlockRendererTypeRegistry;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ClientChunkSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SideOnly(side = Side.CLIENT)
public class ChunkMeshRebuilder implements AsyncOperation, OpenGLOperation {

    /**
     * Parent world
     */
    @NotNull
    private final ClientWorld world;

    /**
     * Queue containing all chunks with unfinished meshes with their builder as it's parent
     */
    @NotNull
    private final Map<ClientChunkSection, OptimizedMeshBuilder> unfinishedMeshes = new HashMap<>();

    /**
     * Stores the registry for block renderers to use for chunk mesh building
     */
    @NotNull
    private final ClientBlockRendererTypeRegistry rendererRegistry;

//    /**
//     * State whether chunk meshes need to be finished on Game Tick
//     */
//    private boolean finishMeshes;

    /**
     * Executor Service to rebuild chunk meshes
     */
    @NotNull
    private final ExecutorService executorService = Executors.newFixedThreadPool(1, r -> {
        Thread thread = new Thread(r, "ChunkRebuilder-PoolThread");
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });

    @NotNull
    private final Lock lock = new ReentrantLock();

    /**
     * @param world sets {@link #world}
     */
    public ChunkMeshRebuilder(@NotNull ClientWorld world, @NotNull ClientBlockRendererTypeRegistry rendererRegistry) {
        this.world = world;
        this.rendererRegistry = rendererRegistry;
    }

    @Override
    @NeedsOpenGLContext
    public void onOpenGLContext(float partialTicks) {
//        if (finishMeshes) {
//            while (!unfinishedMeshes.isEmpty()) {
//                Pair<ClientChunkSection, MeshBuilder> unfinishedSection = unfinishedMeshes.removeFirst();
//                ClientChunkSection section = unfinishedSection.getFirst();
//                MeshBuilder meshBuilder = unfinishedSection.getSecond();
//                section.setMesh(meshBuilder.end());
//                section.setNeedsRebuild(false);
//            }
//            finishMeshes = false;
//        }
    }

    /**
     * Schedules the given chunk to have it's mesh rebuilt
     *
     * @param section      the chunk that needs it's mesh to be rebuilt
     * @param highPriority state whether the chunk should be rebuilt with high priority meaning it is inserted as the first chunk to be rebuilt in the rebuild queue
     * @param onMeshBuilt  invoked on a random thread when the mesh was built (this does not mean it is valid and on the gpu vram already) or null, if no listener is needed
     */
    public void scheduleSectionMeshRebuild(@NotNull ClientChunkSection section, boolean highPriority, @Nullable Runnable onMeshBuilt) {
        // TODO: highpriority doesn't do anything right now...
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            if (section.isEmpty()) {
                section.setShouldHaveMesh(false);
                section.setNeedsRebuild(false);
                return;
            }
            OptimizedMeshBuilder meshBuilder = ChunkMeshBuilder.getPreparedMeshBuilder();
            if (!ChunkMeshBuilder.buildSectionMesh(meshBuilder, rendererRegistry, world, section)) {
                section.setShouldHaveMesh(false);
                section.setNeedsRebuild(false);
                return;
            }

//            Pair<ClientChunkSection, MeshBuilder> unfinishedSection = new Pair<>(section, meshBuilder);

            this.lock.lock();
            this.unfinishedMeshes.put(section, meshBuilder);
            section.setShouldHaveMesh(true);
            this.lock.unlock();

        }, this.executorService).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
        if (onMeshBuilt != null)
            future.thenRunAsync(onMeshBuilt);
    }

    @Override
    public void stopAsyncWork() {
        this.executorService.shutdown();
    }

    @NotNull
    public Optional<OptimizedMeshBuilder> pollMesh(@NotNull ClientChunkSection section) {
        boolean acquired = this.lock.tryLock();
        if (acquired) {
            OptimizedMeshBuilder builder = this.unfinishedMeshes.remove(section);
            this.lock.unlock();
            return Optional.ofNullable(builder);
        } else {
            return Optional.empty();
        }
    }
}
