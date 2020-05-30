package me.gommeantilegit.minecraft.world.chunk;

import com.badlogic.gdx.Gdx;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.annotations.Unsafe;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.block.state.palette.IBlockStatePalette;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.renderer.EntityRenderer;
import me.gommeantilegit.minecraft.rendering.GLContext;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.builder.ChunkMeshRebuilder;
import me.gommeantilegit.minecraft.world.renderer.WorldRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.badlogic.gdx.graphics.GL20.GL_CULL_FACE;
import static com.badlogic.gdx.graphics.GL20.GL_FRONT;
import static me.gommeantilegit.minecraft.world.chunk.ChunkSection.CHUNK_SECTION_SIZE;

public class ClientChunk extends ChunkBase {

    /**
     * State whether the chunk has received any block storage data.
     */
    private boolean dataReceived = false;

    /**
     * The chunk rebuilder used to build chunk section meshes
     */
    @NotNull
    private final ChunkMeshRebuilder chunkMeshRebuilder;

    /**
     * Stores the sections of the chunk that are renderable.
     * Null, if the chunk is not baked. The elements are never null. Fuck you Jetbrains annotations. See bug https://youtrack.jetbrains.com/issue/IDEA-176629
     */
    private ClientChunkSection[] renderableSections = null;

    /**
     * Default constructor of a ChunkBase object
     *
     * @param height             height of the world -> becomes chunk height
     * @param x                  startX position where the region managed by the chunk starts
     * @param z                  startZ position where the region managed by the chunk starts
     * @param world              the parent world
     * @param chunkMeshRebuilder the chunk mesh rebuilder used to build chunk section meshes
     * @param blockStatePalette  the block state palette used to store the chunk
     */
    public ClientChunk(int height, int x, int z, @NotNull ClientWorld world, @NotNull ChunkMeshRebuilder chunkMeshRebuilder, @NotNull IBlockStatePalette blockStatePalette) {
        super(height, x, z, world, new ArrayList<>(height / CHUNK_SECTION_SIZE), blockStatePalette);
        this.chunkMeshRebuilder = chunkMeshRebuilder;
        this.initChunkSections(); // accesses #chunkMeshRebuilder
    }

    @Override
    protected void changeBlock(int x, int y, int z, @Nullable IBlockState blockState) {
        {
            super.changeBlock(x, y, z, blockState);
        }
    }

    @Override
    public void setRelativeBlockState(int x, int y, int z, @Nullable IBlockState blockState) {
        this.blockStateSemaphore.writeSynchronized(this, () -> {
            writeBlockChange(x, y, z, blockState);
            rebuildFor(x, y, z);
        });
    }

    @Override
    public void writeBlockChange(int x, int y, int z, @Nullable IBlockState blockState) {
        super.writeBlockChange(x, y, z, blockState);
    }

    @Unsafe
    @Override
    public void setChunkData(@NotNull byte[] bytes, @NotNull BitSet chunkFragmentsSent) {
        super.setChunkData(bytes, chunkFragmentsSent);
        this.setDataReceived();
        this.scheduleChunkMeshTasks(true);
    }

    /**
     * Schedules the rebuild tasks that arise from a chunk block state change.
     * (Rebuilds neighboring chunks, if this chunk is now the final needed neighbor to build it etc.)
     */
    public void scheduleChunkMeshTasks(boolean descent) {
        // asserts this chunk is locked
        // Make sure all neighbor chunks exist
        ChunkBase[] neighbors = this.getAllNeighbors();

        boolean shouldBuild = true;
        for (ChunkBase chunk : neighbors) {
            ClientChunk neighbor = (ClientChunk) chunk;
            if (neighbor == null || !neighbor.isLoaded() || !neighbor.hasReceivedData()) {
                shouldBuild = false;
                break;
            }
        }
        if (shouldBuild) {
            for (ChunkSection section : this.getChunkSections()) {
                ClientChunkSection clientChunkSection = (ClientChunkSection) section;
                clientChunkSection.scheduleRebuild();
            }
        }
        // check for mesh build on chunks which set of complete neighbors is not becoming complete
        if (descent) {
            for (ChunkBase chunk : neighbors) {
                if (chunk == null)
                    continue;
                ((ClientChunk) chunk).scheduleChunkMeshTasks(false);
            }
        }
    }

    @Override
    protected void forceRemoveEntity(int entityIndex, @NotNull EntityRemoveReason removeReason) {
        super.forceRemoveEntity(entityIndex, removeReason);
    }

    @NotNull
    @Override
    protected ClientChunkSection createChunkSection(int startHeight) {
        return new ClientChunkSection(this, startHeight, chunkMeshRebuilder);
    }

    @Override
    @Nullable
    public ClientChunk getNeighbor(int neighborIndex) {
        return (ClientChunk) super.getNeighbor(neighborIndex);
    }

    @NotNull
    @Unsafe
    public ClientChunkSection getChunkSection(int y) {
        return (ClientChunkSection) super.getChunkSection(y);
    }

    @Override
    protected void transferEntity(@NotNull Entity entity, @NotNull ChunkBase newChunk) {
        super.transferEntity(entity, newChunk);
    }

    /**
     * Renders the given chunk.
     *
     * @param shader         the shader program to render the chunk with
     * @param entityRenderer used to render the entities of the chunk
     * @param partialTicks   delta time
     * @return the number of draw section draw calls it has made
     */
    public int render(@NotNull StdShader shader, @NotNull WorldRenderer worldRenderer, @NotNull EntityRenderer entityRenderer, float partialTicks) {

        Gdx.gl.glCullFace(GL_FRONT); // Resetting culled face to Front face
        Gdx.gl.glDisable(GL_CULL_FACE); // Disable Face culling!!! (Or else white artifacts at T-Junctions will appear)

        shader.pushMatrix();
        shader.translate(x, 0, z);

        int drawCalls = 0;
        List<ChunkSection> renderableSections = getChunkSections();

        for (ChunkSection chunkSection : renderableSections) {
            if (worldRenderer.isChunkInCameraFrustum(chunkSection)) {
                drawCalls += ((ClientChunkSection) chunkSection).render(shader);
            }
        }
        shader.popMatrix();

        this.renderEntities(shader, entityRenderer, partialTicks);
        return drawCalls;
    }


    /**
     * Rebuilds the chunk mesh with the position of the block change in mind
     *
     * @param x change x coordinate
     * @param y change y coordinate
     * @param z change z coordinate
     */
    public void rebuildFor(int x, int y, int z) {
        ClientChunkSection section = getChunkSection(y);
        section.rebuildFor(x, y, z); // auto bakes the chunk
    }

    /**
     * Rebuilds the chunk mesh
     */
    public void rebuild() {
        for (ChunkSection chunkSection : this.getChunkSections()) {
            if (chunkSection instanceof ClientChunkSection) {
                ((ClientChunkSection) chunkSection).scheduleRebuild();
            }
        }
    }

    /**
     * Renders all entities
     *
     * @param partialTicks this performed this frame
     */
    private void renderEntities(@NotNull StdShader shader, @NotNull EntityRenderer entityRenderer, float partialTicks) {
        for (Entity entity : entities) {
            if (entity != null) {
                entityRenderer.renderEntity(entity, partialTicks, shader);
            }
        }
    }

    @NotNull
    @Override
    public ClientWorld getWorld() {
        return (ClientWorld) super.getWorld();
    }

    @Override
    @ThreadSafe
    public void load() {
        {
            super.load();
        }
    }

    @Override
    public void unload() {
        {
            {
                this.dataReceived = false;
            }
            for (ChunkSection chunkSection : getChunkSections()) {
                ClientChunkSection section = (ClientChunkSection) chunkSection;
                getWorld().getChunkMeshRebuilder().cancelRebuildTask(section);
                GLContext.getGlContext().runOnGLContext(section::deleteMesh);
            }
            // canceling build tasks of direct neighbors to prevent mesh corruption
            for (ChunkBase neighbor : this.getPresentNeighbors()) {
                for (ChunkSection chunkSection : neighbor.getChunkSections()) {
                    ClientChunkSection section = (ClientChunkSection) chunkSection;
                    getWorld().getChunkMeshRebuilder().cancelRebuildTask(section);
                    GLContext.getGlContext().runOnGLContext(section::deleteMesh);
                }
            }
            super.unload();
        }
    }

    @Unsafe
    public void setDataReceived() {
        {
            this.dataReceived = true;
        }
    }

    @NotNull
    public ClientChunkSection[] getRenderableSections() {
        return Objects.requireNonNull(renderableSections, "Tried to access renderable chunk sections! Chunk is not baked.");
    }

    /**
     * State whether the server has sent the initial data for the given chunk
     */
    public boolean hasReceivedData() {
        return dataReceived;
    }
}
