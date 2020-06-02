package me.gommeantilegit.minecraft.world.chunk;

import com.badlogic.gdx.graphics.Mesh;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.builder.ChunkMeshRebuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static me.gommeantilegit.minecraft.world.chunk.ChunkBase.CHUNK_SIZE;

public class ClientChunkSection extends ChunkSection {

    /**
     * The mesh parent to the chunk section
     */
    @Nullable
    private Mesh mesh;

    @NotNull
    private final ChunkMeshRebuilder chunkMeshRebuilder;

    public ClientChunkSection(@NotNull ClientChunk parentChunk, int startHeight, @NotNull ChunkMeshRebuilder chunkMeshRebuilder) {
        super(parentChunk, startHeight);
        this.chunkMeshRebuilder = chunkMeshRebuilder;
    }

    /**
     * Renders the chunk section, if it has a mesh. (synchronized with mesh setting... IF THIS ACCESSES DELETED BUFFERS THIS SEG FAULTS)
     *
     * @param shader the shader program to render it with
     */
    public int render(@NotNull StdShader shader) {
        Mesh mesh = retrieveMesh();

        if (mesh == null) {
            return 0;
        }

        shader.pushMatrix();
        shader.translate(0, getStartHeight(), 0);
        mesh.render(shader, GL_TRIANGLES);
        shader.popMatrix();
        return 1;
    }

    @Nullable
    private Mesh retrieveMesh() {
        return this.mesh;
    }

    /**
     * Schedules a rebuild of the neighbor sections of this chunk sections (sections are in this and other chunks) and performs a chunk bake for affected chunks once all meshes have been built
     */
    public void rebuildRelative(int x, int y, int z) {
        ClientChunk parentChunk = getParentChunk();

        ClientWorld world = parentChunk.getWorld();
        int startHeight = getStartHeight();
        int chunkX = parentChunk.getX(), chunkZ = parentChunk.getZ();

        y -= startHeight;

        List<ClientChunkSection> toRebuild = new ArrayList<>(6);
        toRebuild.add(this);

        if (x == 0) {
            ClientChunk chunk = world.getChunkAtOrigin(chunkX - CHUNK_SIZE, chunkZ);
            if (chunk != null) {
                toRebuild.add(chunk.getChunkSection(startHeight));
            }
        }

        if (x == CHUNK_SIZE - 1) {
            ClientChunk chunk = world.getChunkAtOrigin(chunkX + CHUNK_SIZE, chunkZ);
            if (chunk != null) {
                toRebuild.add(chunk.getChunkSection(startHeight));
            }
        }

        if (z == 0) {
            ClientChunk chunk = world.getChunkAtOrigin(chunkX, chunkZ - CHUNK_SIZE);
            if (chunk != null) {
                toRebuild.add(chunk.getChunkSection(startHeight));
            }
        }

        if (z == CHUNK_SIZE - 1) {
            ClientChunk chunk = world.getChunkAtOrigin(chunkX, chunkZ + CHUNK_SIZE);
            if (chunk != null) {
                toRebuild.add(chunk.getChunkSection(startHeight));
            }
        }

        if (y == 0) {
            if (startHeight >= CHUNK_SECTION_SIZE) {
                toRebuild.add(parentChunk.getChunkSection(startHeight - CHUNK_SECTION_SIZE));
            }
        }

        if (y == CHUNK_SECTION_SIZE - 1) {
            if (startHeight < parentChunk.getHeight() - CHUNK_SECTION_SIZE) {
                toRebuild.add(parentChunk.getChunkSection(startHeight + CHUNK_SECTION_SIZE));
            }
        }
        for (ClientChunkSection section : toRebuild) {
            section.scheduleRebuild();
        }
    }

    /**
     * Sets the mesh of the section to the specified mesh and disposes the old mesh of the section, if present
     *
     * @param mesh the new section mesh
     */
    public void setMesh(@Nullable Mesh mesh) {
        if (this.mesh != null && mesh != this.mesh) {
            this.mesh.dispose();
        }
        this.mesh = mesh;
    }


//    private boolean needsRebuild = true;

    /**
     * Schedules a chunk section rebuild on the ChunkRebuilder, if the block change has changed since last invocation
     */
    public void scheduleRebuild() {
//        if (this.needsRebuild) {
        this.chunkMeshRebuilder.scheduleSectionMeshRebuild(this);
//            this.needsRebuild = false;
//        }
    }

    @Override
    public void setBlockState(int x, int y, int z, @Nullable IBlockState blockState) {
        super.setBlockState(x, y, z, blockState);
//        this.needsRebuild = true;
    }

    /**
     * @return true if the section has a mesh. this returns true for un-built sections and empty sections
     */
    public boolean hasMesh() {
        return this.mesh != null;
    }

    /**
     * Deletes and disposes the mesh.
     * {@link #hasMesh()} will return false.
     */
    public void deleteMesh() {
        setMesh(null);
    }

    @NotNull
    @Override
    public ClientChunk getParentChunk() {
        return (ClientChunk) super.getParentChunk();
    }

    public void onMeshBuildCancelled() {
    }

    public void onMeshBuildComplete() {
    }


}
