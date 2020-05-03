package me.gommeantilegit.minecraft.world.chunk.builder;

import com.badlogic.gdx.Gdx;
import me.gommeantilegit.minecraft.block.ClientBlockRendererTypeRegistry;
import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ClientChunkSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static me.gommeantilegit.minecraft.rendering.Constants.STD_VERTEX_ATTRIBUTES;
import static me.gommeantilegit.minecraft.world.chunk.ChunkBase.CHUNK_SIZE;
import static me.gommeantilegit.minecraft.world.chunk.ChunkSection.CHUNK_SECTION_SIZE;

public class ChunkMeshBuilder {

    /**
     * Builds a mesh for a chunk section
     *
     * @param builder          the mesh-builder to store the finished mesh
     * @param rendererRegistry the block render registry
     * @param world            the world to use for neighbor face checking
     * @param section          the section to build a mesh for
     * @return true, if at least one face was rendered (well, it should in theory be at least 6 faces or 0 nothing in between but ok)
     */
    public static boolean buildSectionMesh(@NotNull OptimizedMeshBuilder builder, @NotNull ClientBlockRendererTypeRegistry rendererRegistry, @NotNull WorldBase world, @NotNull ClientChunkSection section) {
        long start = System.currentTimeMillis();
        boolean renderedFace = false;
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SECTION_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    IBlockState blockState = section.getRelativeBlockState(x, y, z);
                    if (blockState != null) {
                        if (Objects.requireNonNull(rendererRegistry.getRenderer(blockState.getBlock())).render(builder, x, y, z, section, world, blockState, false)) {
                            renderedFace = true;
                        }
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
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
