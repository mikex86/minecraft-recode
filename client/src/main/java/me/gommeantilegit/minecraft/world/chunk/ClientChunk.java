package me.gommeantilegit.minecraft.world.chunk;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.block.ClientBlock;
import me.gommeantilegit.minecraft.block.ClientBlocks;
import me.gommeantilegit.minecraft.block.state.ClientBlockState;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.utils.Clock;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.loader.ClientChunkLoader;
import me.gommeantilegit.minecraft.world.chunk.world.ClientWorldChunkHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static me.gommeantilegit.minecraft.rendering.Constants.STD_VERTEX_ATTRIBUTES;

public class ClientChunk extends ChunkBase<ClientChunkSection, ClientChunk, ClientWorldChunkHandler, ClientBlocks, ClientMinecraft, ClientBlock, ClientBlockState, ClientWorld, ClientChunkLoader> {

    /**
     * The mesh of the chunk being rendered.
     */
    @Nullable
    private Mesh mesh;

    /**
     * State whether the chunk should be rebuilt the next time it is rendered.
     */
    private boolean rebuild = false;

    /**
     * State whether the upcoming data transmission of the chunks initial data has been confirmed by the server
     */
    private boolean requestConfirmed = false;

    /**
     * State whether the server has sent the initial data for the given chunk
     */
    public boolean dataReceived = false;

    /**
     * Clock instance to time how long a chunk's data has been requested
     */
    @NotNull
    private final Clock dataRequestedTimer = new Clock(false);

    @NotNull
    private final ClientMinecraft mc;

    /**
     * Default constructor of a ChunkBase object
     *
     * @param height height of the world -> becomes chunk height
     * @param x      startX position where the region managed by the chunk starts
     * @param z      startZ position where the region managed by the chunk starts
     * @param world  the parent world
     */
    public ClientChunk(int height, int x, int z, @NotNull ClientWorld world) {
        super(height, x, z, world, ClientBlockState.class, ClientChunkSection.class);
        this.mc = world.mc;
    }

    @Override
    protected void changeBlock(int x, int y, int z, @Nullable ClientBlockState blockState) {
        super.changeBlock(x, y, z, blockState);
        this.world.rebuildChunksFor(x, z, true);
    }

    /**
     * Rebuilds the chunk mesh.
     */
    public void rebuild() {
        MeshBuilder builder = buildChunkMesh();
        if (this.mesh != null) this.mesh.dispose();
        this.mesh = builder.end();
    }

    @Override
    protected void removeEntity(int entityIndex, @NotNull EntityRemoveReason removeReason) {
        super.removeEntity(entityIndex, removeReason);
    }

    /**
     * @return a mesh-builder with the stored chunk in it.
     */
    public MeshBuilder buildChunkMesh() {

        MeshBuilder builder = new MeshBuilder();
        builder.ensureCapacity(
                (CHUNK_SIZE * CHUNK_SIZE * height * 4 * 4) / 2,
                (CHUNK_SIZE * CHUNK_SIZE * height * 4 * 6) / 2
        );
        builder.begin(STD_VERTEX_ATTRIBUTES, GL_TRIANGLES);
        for (int x = this.x; x < this.x + CHUNK_SIZE; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = this.z; z < this.z + CHUNK_SIZE; z++) {
                    ClientBlockState blockState = getBlockState(x, y, z);
                    if (blockState != null) {
                        blockState.getBlock().render(builder, x - this.x, y, z - this.z, x, y, z, world, blockState, false);
                    }
                }
            }
        }
        return builder;
    }

    @NotNull
    @Override
    protected ClientChunkSection getChunkSection(int startHeight) {
        return new ClientChunkSection(this, startHeight);
    }

    @Override
    public void forceAddEntity(@NotNull Entity entity) {
        super.forceAddEntity(entity);
    }

    /**
     * Sets the state if the chunk should be rebuilt next time it is rendered.
     *
     * @param rebuild state if the chunk should be rebuilt.
     * @param highPriority state whether the chunk should be the first in the rebuild queue
     * @return self instance (Builder function)
     */
    public ChunkBase setNeedsRebuild(boolean rebuild, boolean highPriority) {
        if (rebuild && !this.rebuild)
            this.mc.theWorld.getWorldChunkHandler().chunkRebuilder.scheduleRebuild(this, highPriority);
        this.rebuild = rebuild;
        return this;
    }

    /**
     * Renders the given chunk.
     *
     * @param partialTicks delta time
     */
    public void render(float partialTicks) {
        mc.shaderManager.stdShader.pushMatrix();
        mc.shaderManager.stdShader.translate(x, 0, z);
        if (mesh != null)
            this.mesh.render(mc.shaderManager.stdShader, GL_TRIANGLES);
        mc.shaderManager.stdShader.popMatrix();
        this.renderEntities(partialTicks);
    }

    /**
     * Renders all entities
     *
     * @param partialTicks this performed this frame
     */
    private void renderEntities(float partialTicks) {
        for (int i = 0; i < entities.size(); i++) {
            try {
                Entity entity = entities.get(i);
                if (entity != null) {
                    mc.entityRenderer.renderEntity(entity, partialTicks, mc.shaderManager.stdShader);
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
    }

    /**
     * @return the state if the chunk should be rebuilt.
     */
    public boolean needsRebuild() {
        return rebuild;
    }

    /**
     * Nullifies the variable {@link #mesh}
     */
    public void nullifyMesh() {
        this.mesh = null;
    }

    public ChunkBase setMesh(@Nullable Mesh mesh) {

        this.mesh = mesh;

        return this;
    }

    @NotNull
    @Override
    public ClientWorld getWorld() {
        return super.getWorld();
    }

    @Nullable
    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public void load() {
        if (mesh == null && dataReceived)
            setNeedsRebuild(true, false);
        super.load();
    }

    public void setRequestedConfirmed() {
        this.requestConfirmed = true;
    }

    public void setDataRequested() {
        this.dataRequestedTimer.reset();
    }

    @NotNull
    public Clock getDataRequestedTimer() {
        return dataRequestedTimer;
    }

    public boolean isRequestConfirmed() {
        return rebuild;
    }
}