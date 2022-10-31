package me.gommeantilegit.minecraft.world.chunk.builder;

import me.gommeantilegit.minecraft.block.ClientBlockRendererTypeRegistry;
import me.gommeantilegit.minecraft.block.access.IReadableBlockStateAccess;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Represents a task that builds a chunk mesh.
 * The task returns the mesh builder containing the finished mesh, ready to be finished into a mesh - or NULL if no mesh is needed.
 */
public class ChunkMeshingTask implements Supplier<OptimizedMeshBuilder> {

    /**
     * Provides block state data to build the mesh of
     */
    @NotNull
    private final IReadableBlockStateAccess blockStateAccess;

    /**
     * The starting position of the region to build the mesh for
     * Defines (x0, y0, z0)
     */
    @NotNull
    private final Vec3i startPos;

    /**
     * The end position of the region to build the mesh for
     * Defines (x1, y1, z1)
     */
    @NotNull
    private final Vec3i endPos;

    /**
     * The block render type registry that builds the block individual mesh parts
     */
    @NotNull
    private final ClientBlockRendererTypeRegistry renderRegistry;

    public ChunkMeshingTask(@NotNull IReadableBlockStateAccess blockStateAccess,
                            @NotNull Vec3i startPos, @NotNull Vec3i endPos, @NotNull ClientBlockRendererTypeRegistry renderRegistry) {
        this.blockStateAccess = blockStateAccess;
        this.startPos = startPos;
        this.endPos = endPos;
        this.renderRegistry = renderRegistry;
    }

    @Nullable
    @Override
    public OptimizedMeshBuilder get() {
        OptimizedMeshBuilder meshBuilder = ChunkMeshBuilder.getPreparedMeshBuilder();
        boolean renderedFace = ChunkMeshBuilder.buildMesh(meshBuilder, this.renderRegistry, this.blockStateAccess, this.startPos, this.endPos);
        if (!renderedFace) {
            return null;
        }
        return meshBuilder;
    }


}
