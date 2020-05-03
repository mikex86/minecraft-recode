package me.gommeantilegit.minecraft.rendering.mesh;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.utils.async.AsyncExecutor;
import me.gommeantilegit.minecraft.utils.async.AsyncResult;
import me.gommeantilegit.minecraft.utils.async.Invokable;
import me.gommeantilegit.minecraft.utils.async.ListenableFuture;
import me.gommeantilegit.minecraft.world.chunk.builder.OptimizedMeshBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that has a mesh that has to be built asynchronously
 * <p>
 * To build the mesh call {@link #setupMeshAsync(Invokable)}} ()}
 * To finish the MeshBuilder into a mesh ready to be rendered call {@link #finishMesh()} on the OpenGL Context
 */
public interface MeshBuilding {

    @NotNull
    AsyncExecutor asyncExecutor = new AsyncExecutor(32);

    /**
     * Called to build the mesh
     *
     * @return the meshbuilder that the mesh was built into
     */
    @NotNull
    OptimizedMeshBuilder buildMesh();

    /**
     * Stores the built mesh that is ready to be converted into a mesh
     *
     * @param meshBuilder the just built mesh-builder
     */
    void storeBuildMesh(@NotNull OptimizedMeshBuilder meshBuilder);

    /**
     * Called to set up the mesh builder that is later converted into a mesh.
     * Works asynchronously
     */
    default AsyncResult<Void> setupMeshAsync(@NotNull Invokable<ListenableFuture<Void>> invokable) {
        return asyncExecutor.submit(() -> {
            storeBuildMesh(buildMesh());
            return null;
        }, invokable);
    }

    /**
     * Sets up the mesh synchronously
     */
    default void setupMesh() {
        storeBuildMesh(buildMesh());
    }

    /**
     * Called to finish the built mesh.
     * Needs OpenGL context as the mesh is uploaded to the GPU
     *
     * @param meshBuilder the mesh-builder containing the built mesh ready to be finished
     */
    @NeedsOpenGLContext
    void finishMesh(@NotNull OptimizedMeshBuilder meshBuilder);

    /**
     * Wrapper function version of {@link #finishMesh(OptimizedMeshBuilder)}
     */
    @NeedsOpenGLContext
    void finishMesh();
}
