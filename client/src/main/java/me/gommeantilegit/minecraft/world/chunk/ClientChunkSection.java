package me.gommeantilegit.minecraft.world.chunk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.builder.ChunkMeshRebuilder;
import me.gommeantilegit.minecraft.world.chunk.builder.OptimizedMeshBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static me.gommeantilegit.minecraft.world.chunk.ChunkBase.CHUNK_SIZE;

public class ClientChunkSection extends ChunkSection {

    /**
     * The mesh parent to the chunk section
     */
    @Nullable
    private Mesh mesh;

    /**
     * State of the chunk needing to be rebuilt
     */
    private boolean needsRebuild = false;

    @NotNull
    private final ChunkMeshRebuilder chunkMeshRebuilder;

    /**
     * State whether the section should have a mesh
     */
    private boolean shouldHaveMesh = false;

    public ClientChunkSection(@NotNull ClientChunk parentChunk, int startHeight, @NotNull ChunkMeshRebuilder chunkMeshRebuilder) {
        super(parentChunk, startHeight);
        this.chunkMeshRebuilder = chunkMeshRebuilder;
    }

    /**
     * Renders the chunk section, if it has a mesh
     *
     * @param shader the shader program to render it with
     */
    public int render(@NotNull StdShader shader) {
        if (this.needsRebuild || this.mesh == null) {
            boolean state = getParentChunk().getWorld().getRenderManager().canUpload();
            if (state) {
                long start = System.currentTimeMillis();
                Optional<OptimizedMeshBuilder> result = this.chunkMeshRebuilder.pollMesh(this);
                if (!result.isPresent()) {
                    if (this.mesh == null) {
                        return 0;
                    }
                } else {
                    this.mesh = result.get().end();
                    Objects.requireNonNull(this.mesh, "MeshBuilder built section mesh null!");
                    this.needsRebuild = false;
                    long end = System.currentTimeMillis();
                    if ((end - start) > 0) {
                        System.out.println("upload took: " + (end - start) + " ms");
                    }
                }
            } else {
                if (this.mesh == null) {
                    return 0;
                }
            }
        }

        shader.pushMatrix();
        shader.translate(0, getStartHeight(), 0);
        this.mesh.render(shader, GL_TRIANGLES);
        shader.popMatrix();
        return 1;
    }

    /**
     * Schedules a rebuild of the neighbor sections of this chunk sections (sections are in this and other chunks) and performs a chunk bake for affected chunks once all meshes have been built
     */
    //TODO: ALSO DOESN'T SEEM THAT THREAD-SAFE TO ME... INVESTIGATE FURTHER
    public void rebuildFor(@NotNull ClientMinecraft mc, boolean highPriority, int x, int y, int z) {
        ClientChunk parentChunk = getParentChunk();
        parentChunk.unbake();

        ClientWorld world = parentChunk.getWorld();
        int startHeight = getStartHeight();
        int chunkX = parentChunk.getX(), chunkZ = parentChunk.getZ();
        x -= chunkX;
        y -= startHeight;
        z -= chunkZ;
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
        AtomicInteger counter = new AtomicInteger();
        for (ClientChunkSection section : toRebuild) {
            {
                Set<ClientChunk> uniqueChunks = new HashSet<>();
                for (ClientChunkSection chunkSection : toRebuild) {
                    uniqueChunks.add(chunkSection.getParentChunk());
                }
                for (ClientChunk uniqueChunk : uniqueChunks) {
                    uniqueChunk.unbake();
                }
            }
            section.setNeedsRebuild(true, true, () -> {
                if (counter.incrementAndGet() == toRebuild.size()) {
                    Set<ClientChunk> uniqueChunks = new HashSet<>();
                    for (ClientChunkSection chunkSection : toRebuild) {
                        uniqueChunks.add(chunkSection.getParentChunk());
                    }
                    for (ClientChunk chunk : uniqueChunks) {
                        chunk.bake();
                    }
                }
            });
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

    /**
     * Sets the state of the section needing a rebuild. This will schedule a chunk section rebuild on the ChunkRebuilder. If the supplied state is equal to the current state, the call will be ignored.
     *
     * @param needsRebuild the new state
     * @param highPriority the state whether the chunk section should be added first in queue to be rebuilt
     */
    @ThreadSafe
    public void setNeedsRebuild(boolean needsRebuild, boolean highPriority) {
        if (this.needsRebuild == needsRebuild) return;
        this.needsRebuild = needsRebuild;
        if (needsRebuild)
            this.chunkMeshRebuilder.scheduleSectionMeshRebuild(this, highPriority, null);
    }

    /**
     * Sets the state of the section needing a rebuild. This will schedule a chunk section rebuild on the ChunkRebuilder. If the supplied state is equal to the current state, the call will be ignored.
     *
     * @param needsRebuild the new state
     * @param highPriority the state whether the chunk section should be added first in queue to be rebuilt
     * @param onMeshBuilt  invoked on a random thread when the mesh was built (this does not mean it is valid and on the gpu vram already) or null, if no listener is needed
     */
    @ThreadSafe
    public void setNeedsRebuild(boolean needsRebuild, boolean highPriority, @NotNull Runnable onMeshBuilt) {
        if (this.needsRebuild == needsRebuild) return;
        this.needsRebuild = needsRebuild;
        if (needsRebuild)
            this.chunkMeshRebuilder.scheduleSectionMeshRebuild(this, highPriority, onMeshBuilt);
    }

    /**
     * Sets the state of the section needing a rebuild. This will schedule a chunk section rebuild on the ChunkRebuilder. If the supplied state is equal to the current state, the call will be ignored.
     *
     * @param needsRebuild the new state
     */
    public void setNeedsRebuild(boolean needsRebuild) {
        setNeedsRebuild(needsRebuild, false);
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
    @ThreadSafe
    public void deleteMesh() {
        setMesh(null);
    }

    @NotNull
    @Override
    public ClientChunk getParentChunk() {
        return (ClientChunk) super.getParentChunk();
    }

    /**
     * @return true if the the section should have a mesh. if {@link #hasMesh()} is true, this should always return true. This is the state of the mesh actually containing a face even before it is uploaded to the GPU vRam and thus available in {@link #mesh}. This is instantly available after the mesh rebuild was performed (asynchronously)
     */
    public boolean shouldHaveMesh() {
        return shouldHaveMesh;
    }

    public void setShouldHaveMesh(boolean shouldHaveMesh) {
        this.shouldHaveMesh = shouldHaveMesh;
    }
}
