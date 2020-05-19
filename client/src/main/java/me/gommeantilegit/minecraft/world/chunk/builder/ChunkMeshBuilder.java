package me.gommeantilegit.minecraft.world.chunk.builder;

import me.gommeantilegit.minecraft.block.ClientBlockRendererTypeRegistry;
import me.gommeantilegit.minecraft.block.access.IReadableBlockStateAccess;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.block.state.storage.BlockStateStorage;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec3i;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import me.gommeantilegit.minecraft.world.chunk.ClientChunkSection;
import me.gommeantilegit.minecraft.world.chunk.builder.exception.ChunkMeshNoLongerNeededException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static me.gommeantilegit.minecraft.rendering.Constants.STD_VERTEX_ATTRIBUTES;
import static me.gommeantilegit.minecraft.world.chunk.ChunkBase.CHUNK_SIZE;
import static me.gommeantilegit.minecraft.world.chunk.ChunkSection.CHUNK_SECTION_SIZE;

public class ChunkMeshBuilder {

    /**
     * Builds a mesh for given region in a block state access provider
     *
     * @param builder          the mesh-builder to store the finished mesh
     * @param rendererRegistry the block render registry
     * @param access           provides the block states to build the mesh of
     * @return true, if at least one face was rendered (well, it should in theory be at least 6 faces or 0 nothing in between but ok)
     */
    public static boolean buildMesh(@NotNull OptimizedMeshBuilder builder, @NotNull ClientBlockRendererTypeRegistry rendererRegistry, @NotNull IReadableBlockStateAccess access, @NotNull Vec3i startPosition, @NotNull Vec3i endPosition) {
        int x0 = startPosition.getX(), y0 = startPosition.getY(), z0 = startPosition.getZ();
        int x1 = endPosition.getX(), y1 = endPosition.getY(), z1 = endPosition.getZ();

//        long start = System.currentTimeMillis();
        boolean renderedFace = false;
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    IBlockState blockState = access.getBlockState(x, y, z);
                    if (blockState != null) {
                        if (Objects.requireNonNull(rendererRegistry.getRenderer(blockState.getBlock())).render(builder, x, y, z, access, blockState)) {
                            renderedFace = true;
                        }
                    }
                }
            }
        }
//        long end = System.currentTimeMillis();
//        if ((end - start) > 0)
//            System.out.println("buildSectionMesh Took: " + (end - start) + " ms");
        return renderedFace;
    }

    @NotNull
    public static OptimizedMeshBuilder getPreparedMeshBuilder() {
        OptimizedMeshBuilder builder = new OptimizedMeshBuilder();
        builder.begin(STD_VERTEX_ATTRIBUTES, GL_TRIANGLES);
        builder.ensureCapacity(
                // the most efficient way to use as many vertices as possible is to do a checkered pattern in 3d space
                (CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE * 4 * 4) / 2,
                (CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE * 4 * 6) / 2
        );
        return builder;
    }

}
