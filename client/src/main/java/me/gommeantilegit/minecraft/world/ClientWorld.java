package me.gommeantilegit.minecraft.world;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.particle.ParticleEngine;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerSP;
import me.gommeantilegit.minecraft.packet.packets.client.ClientChunkLoadingDistanceChangePacket;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import me.gommeantilegit.minecraft.world.chunk.builder.ChunkRebuilder;
import me.gommeantilegit.minecraft.world.chunk.creator.ClientChunkCreator;
import me.gommeantilegit.minecraft.world.chunk.creator.OnChunkCreationListener;
import me.gommeantilegit.minecraft.world.chunk.loader.ClientChunkLoader;
import me.gommeantilegit.minecraft.world.chunk.world.ChunkRenderManager;
import me.gommeantilegit.minecraft.world.chunk.world.ClientWorldChunkHandler;
import me.gommeantilegit.minecraft.world.renderer.WorldRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static me.gommeantilegit.minecraft.Side.CLIENT;

@SideOnly(side = CLIENT)
public class ClientWorld extends WorldBase implements OpenGLOperation {

    /**
     * Object for rendering the world
     */
    @NotNull
    private final WorldRenderer worldRenderer;

    /**
     * The object rebuildint chunk meshes
     */
    @NotNull
    private final ChunkRebuilder chunkRebuilder;

    /**
     * The chunk rendering manager that prepares a iteration over the chunks to render.
     */
    @NotNull
    public final ChunkRenderManager chunkRenderManager;

    /**
     * The viewer of the single player world that is the player playing the game on this computer
     */
    @NotNull
    public final EntityPlayerSP viewer;

    /**
     * Object used for spawning particle structures into the world.
     */
    public ParticleEngine particleEngine;

    /**
     * Distance from viewer to chunk that the chunk is considered a near chunk
     */
    private int nearChunkDistance;

    public ClientWorld(@NotNull EntityPlayerSP viewer, @NotNull ClientMinecraft mc, int worldHeight) {
        super(mc, worldHeight);
        this.worldThread = new Thread(this, "ClientWorld-thread");
        this.worldChunkHandler = new ClientWorldChunkHandler(this);
        this.worldRenderer = new WorldRenderer(this, viewer, mc);
        this.chunkRenderManager = new ChunkRenderManager(this);
        this.chunkCreator = new ClientChunkCreator(mc, this);
        this.chunkLoader = new ClientChunkLoader(this, mc);
        this.chunkRebuilder = new ChunkRebuilder(this);
        this.particleEngine = new ParticleEngine(mc);
        this.viewer = viewer;
        this.modifyChunkLoadingDistance(mc.gameSettings.videoSettings.determineChunkLoadingDistance());
        this.nearChunkDistance = getChunkLoadingDistance() * 2;

        // MUST BE THE LAST THING TO PERFORM IN WORLD CONSTRUCTOR
        this.worldThread.setDaemon(true);
        this.worldThread.start();
    }

    @Override
    @NeedsOpenGLContext
    public void onOpenGLContext(float partialTicks) {
        ((ClientWorldChunkHandler) this.worldChunkHandler).getChunkRebuilder().onOpenGLContext(partialTicks);
        this.particleEngine.onOpenGLContext(partialTicks);
        this.worldRenderer.onOpenGLContext(partialTicks);
    }

    /**
     * Invalidates all meshes of the chunks
     */
    public void invalidateMeshes() {
        for (ChunkBase chunkBase : this.worldChunkHandler.getChunks()) {
            ClientChunk chunk = (ClientChunk) chunkBase;
            if (chunk.getMesh() != null)
                chunk.getMesh().dispose();
            chunk.setNeedsRebuild(true, false);
        }
    }

    /**
     * Rebuilds all chunks
     */
    public void rebuildAllChunks() {
        for (ChunkBase chunkBase : this.worldChunkHandler.getChunks()) {
            ClientChunk chunk = (ClientChunk) chunkBase;
            chunk.rebuild();
        }
    }

    /**
     * Renders the world by calling {@link WorldRenderer#render(float)}
     *
     * @param partialTicks amount of ticks performed in the current frame
     */
    @NeedsOpenGLContext
    public void render(float partialTicks) {
        this.worldRenderer.render(partialTicks);
        this.chunkRebuilder.onOpenGLContext(partialTicks);
    }

    @Override
    public void onAsyncThread() {
        this.chunkLoader.onAsyncThread();
        this.chunkCreator.onAsyncThread();
        this.worldRenderer.onAsyncThread();
    }

    @NotNull
    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
    }

    /**
     * Rebuilds all chunks partially rendering the block at the specified position
     *
     * @param x            blockX coordinate
     * @param z            blockZ coordinate
     * @param highPriority state whether the chunks should be the first to be built on the ChunkRebuilder
     */
    public void rebuildChunksFor(int x, int z, boolean highPriority) {
        ArrayList<ChunkBase> chunks = new ArrayList<>();

        for (int xo = -1; xo <= 1; xo++) {
            int zo;
            if (xo == 0)
                for (zo = -1; zo <= 1; zo++) {
                    ChunkBase chunk = getChunkForPosition(x + xo, z + zo);
                    if (chunk != null && !chunks.contains(chunk)) chunks.add(chunk);
                }
            else {
                zo = 0;
                ChunkBase chunk = getChunkForPosition(x + xo, z + zo);
                if (chunk != null && !chunks.contains(chunk)) chunks.add(chunk);
            }
        }

        for (ChunkBase chunkBase : chunks) {
            ClientChunk chunk = (ClientChunk) chunkBase;
            chunk.setNeedsRebuild(true, highPriority);
            if (!chunk.isLoaded())
                chunk.load();
        }
    }

    @Override
    public void spawnEntityInWorld(Entity entity) {
        chunkCreator.submit(entity); // Submitting the entity to the chunk creator
    }

    @Override
    public void stopAsyncWork() {
        super.stopAsyncWork();
        this.particleEngine.stopAsyncWork();
        this.chunkRebuilder.stopAsyncWork();
    }

    /**
     * Sets the chunkLoadingDistance for the world object and sends a change packet to the server informing it that the
     * render distance has changed
     *
     * @param chunkLoadingDistance the chunk loading distance
     */
    @Override
    public void setChunkLoadingDistance(int chunkLoadingDistance) {
        super.setChunkLoadingDistance(chunkLoadingDistance);
        ((ClientMinecraft)this.mc).nettyClient.sendPacket(new ClientChunkLoadingDistanceChangePacket(null, chunkLoadingDistance));
        this.nearChunkDistance = getChunkLoadingDistance() * 2;
    }

    /**
     * Sets the chunkLoadingDistance for the world object without sending a change packet to the server
     *
     * @param chunkLoadingDistance the new chunk loading distance packet
     */
    public void modifyChunkLoadingDistance(int chunkLoadingDistance) {
        super.setChunkLoadingDistance(chunkLoadingDistance);
        this.nearChunkDistance = getChunkLoadingDistance() * 2;
    }

    @NotNull
    @Override
    public ClientWorldChunkHandler getWorldChunkHandler() {
        return (ClientWorldChunkHandler) super.getWorldChunkHandler();
    }

    public int getNearChunkDistance() {
        return nearChunkDistance;
    }

    public interface OnClientChunkCreationListener extends OnChunkCreationListener {
    }
}
