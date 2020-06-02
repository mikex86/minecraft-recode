package me.gommeantilegit.minecraft.rendering.mesh;

import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.world.chunk.builder.OptimizedMeshBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that has a mesh that needs to be built
 */
public interface MeshBuilding {

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
