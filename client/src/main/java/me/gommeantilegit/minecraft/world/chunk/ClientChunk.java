package me.gommeantilegit.minecraft.world.chunk;

import com.badlogic.gdx.Gdx;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.renderer.EntityRenderer;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.builder.ChunkMeshRebuilder;
import me.gommeantilegit.minecraft.world.renderer.WorldRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import static com.badlogic.gdx.graphics.GL20.*;
import static me.gommeantilegit.minecraft.world.chunk.ChunkSection.CHUNK_SECTION_SIZE;

public class ClientChunk extends ChunkBase {

    /**
     * State whether the server has sent the initial data for the given chunk
     */
    public boolean dataReceived = false;

    /**
     * The chunk rebuilder used to build chunk section meshes
     */
    @NotNull
    private final ChunkMeshRebuilder chunkMeshRebuilder;

    /**
     * Stores the sections of the chunk that are renderable.
     * Null, if the chunk is not baked.
     */
    @Nullable
    private ClientChunkSection[] renderableSections = null;

    /**
     * State of the chunk being baked.
     *
     * @see #renderableSections
     */
    private boolean baked = false;

    /**
     * Default constructor of a ChunkBase object
     *
     * @param height             height of the world -> becomes chunk height
     * @param x                  startX position where the region managed by the chunk starts
     * @param z                  startZ position where the region managed by the chunk starts
     * @param world              the parent world
     * @param chunkMeshRebuilder the chunk mesh rebuilder used to build chunk section meshes
     */
    public ClientChunk(int height, int x, int z, @NotNull ClientWorld world, @NotNull ChunkMeshRebuilder chunkMeshRebuilder) {
        super(height, x, z, world, new ArrayList<>(height / CHUNK_SECTION_SIZE));
        this.chunkMeshRebuilder = chunkMeshRebuilder;
        this.initChunkSections(); // accesses #chunkMeshRebuilder
    }

    @Override
    protected void changeBlock(int x, int y, int z, @Nullable IBlockState blockState) {
        super.changeBlock(x, y, z, blockState);
        rebuildFor(x, y, z);
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

    @NotNull
    protected ClientChunkSection getChunkSection(int y) {
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
        if (this.baked) {
            ClientChunkSection[] renderableSections = getRenderableSections();

            for (ClientChunkSection chunkSection : renderableSections) {
                if (worldRenderer.isChunkInCameraFrustum(chunkSection)) {
                    drawCalls += chunkSection.render(shader);
                }
            }
        } else {
            List<ChunkSection> renderableSections = getChunkSections();

            for (ChunkSection chunkSection : renderableSections) {
                if (worldRenderer.isChunkInCameraFrustum(chunkSection)) {
                    drawCalls += ((ClientChunkSection) chunkSection).render(shader);
                }
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
        section.rebuildFor((ClientMinecraft) mc, true, x, y, z); // auto bakes the chunk
    }

    /**
     * Rebuilds the chunk mesh
     */
    public void rebuild() {
        //TODO: CHUNK ENTITY TRANSFER AND CHUNK REBUILDING ARE SOURCES OF LAG
        int nSections = this.getChunkSections().size();
        AtomicInteger sectionCounter = new AtomicInteger(0);
        for (ChunkSection chunkSection : this.getChunkSections()) {
            if (chunkSection instanceof ClientChunkSection) {
                ((ClientChunkSection) chunkSection).setNeedsRebuild(true, true, () -> {
                    if (sectionCounter.incrementAndGet() >= nSections) {
                        ((ClientMinecraft) mc).runOnGLContext(new FutureTask<Void>(() -> {
                            this.bake();
                            return null;
                        }));
                    }
                });
            }
        }
    }

    /**
     * Bakes the chunk changes
     */
    public void bake() {
        List<ChunkSection> sections = this.getChunkSections();
        List<ClientChunkSection> toRender = new ArrayList<>();
        for (ChunkSection chunkSection : sections) {
            if (!(chunkSection instanceof ClientChunkSection)) {
                throw new IllegalStateException();
            }
            if (((ClientChunkSection) chunkSection).shouldHaveMesh()) {
                toRender.add((ClientChunkSection) chunkSection);
            }
        }
        this.renderableSections = toRender.toArray(new ClientChunkSection[0]);
        this.baked = true;
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
        super.load();
    }

    @ThreadSafe
    public void setDataReceived() {
        this.dataReceived = true;
        rebuild();
    }

    @NotNull
    public ClientChunkSection[] getRenderableSections() {
        return Objects.requireNonNull(renderableSections, "Tried to access renderable chunk sections! Chunk is not baked.");
    }

    public void unbake() {
        this.baked = false;
    }
}
