package me.gommeantilegit.minecraft.entity.mesh;

import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.rendering.GLContext;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EntityMeshSetupHandler implements AsyncOperation {

    @NotNull
    private final WorldBase world;

    /**
     * ExecutorService to build particle meshes
     */
    @NotNull
    private final ExecutorService executorService = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 3), r -> {
        Thread thread = new Thread(r, "EntityMeshSetupHandler-PoolThread");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        return thread;
    });

    public EntityMeshSetupHandler(@NotNull WorldBase world) {
        this.world = world;
    }

    public void scheduleMeshCreation(@NotNull MeshBuildingEntity entity, @NotNull Consumer<Entity> finishedListener) {
        this.executorService.submit(() -> {
            entity.setupMesh(); // Mesh building complete before it gets added to the queue
            GLContext.getGlContext().runOnGLContext(() -> {
                entity.finishMesh();
                finishedListener.accept(entity); // Mesh initialized before it gets spawned into the world and thus before it is rendered
            });
        });
    }

    @Override
    public void stopAsyncWork() {
        this.executorService.shutdown();
    }
}
