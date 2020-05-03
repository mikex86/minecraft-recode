package me.gommeantilegit.minecraft.world;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.particle.ParticleEngine;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerSP;
import me.gommeantilegit.minecraft.logging.crash.CrashReport;
import me.gommeantilegit.minecraft.packet.packets.client.ClientChunkLoadingDistanceChangePacket;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import me.gommeantilegit.minecraft.world.chunk.builder.ChunkMeshRebuilder;
import me.gommeantilegit.minecraft.world.chunk.creator.ClientChunkCreator;
import me.gommeantilegit.minecraft.world.chunk.creator.OnChunkCreationListener;
import me.gommeantilegit.minecraft.world.chunk.loader.ClientChunkLoader;
import me.gommeantilegit.minecraft.world.chunk.world.RenderManager;
import me.gommeantilegit.minecraft.world.chunk.world.ClientWorldChunkHandler;
import me.gommeantilegit.minecraft.world.renderer.WorldRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.Stream;

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
    private final ChunkMeshRebuilder chunkMeshRebuilder;

    @NotNull
    private final RenderManager renderManager;

    @NotNull
    private final EntityPlayerSP viewer;

    private ParticleEngine particleEngine;

    public ClientWorld(@NotNull EntityPlayerSP viewer, @NotNull ClientMinecraft mc, int worldHeight) {
        super(mc, worldHeight);
        this.worldRenderer = new WorldRenderer(this, viewer, mc, mc.shaderManager.stdShader, mc.textureManager, mc.entityRenderer);
        this.renderManager = new RenderManager(this);
        this.chunkCreator = new ClientChunkCreator(mc, this);
        this.worldChunkHandler = new ClientWorldChunkHandler(chunkCreator, this, mc.blockRendererRegistry);
        this.chunkLoader = new ClientChunkLoader(this, mc);
        this.chunkMeshRebuilder = new ChunkMeshRebuilder(this, mc.blockRendererRegistry);
        this.setParticleEngine(new ParticleEngine(mc));
        this.viewer = viewer;
        this.modifyChunkLoadingDistance(mc.gameSettings.videoSettings.determineChunkLoadingDistance());
    }

    @Override
    @NeedsOpenGLContext
    public void onOpenGLContext(float partialTicks) {
        ((ClientWorldChunkHandler) this.worldChunkHandler).getChunkMeshRebuilder().onOpenGLContext(partialTicks);
        this.getParticleEngine().onOpenGLContext(partialTicks);
        this.worldRenderer.onOpenGLContext(partialTicks);
    }

    /**
     * Invalidates all meshes of the chunks
     */
    public void invalidateMeshes() {
        Iterable<ChunkBase> chunks = this.worldChunkHandler.collectChunks();

        for (ChunkBase chunkBase : chunks) {
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
        this.chunkMeshRebuilder.onOpenGLContext(partialTicks); // TODO: REMOVE
    }

    @Override
    public void tick(float partialTicks) {
        super.tick(partialTicks);
    }

//    /**
//     * ForkJoin for chunk ticking
//     */
//    @NotNull
//    private ForkJoinPool chunkTickPool = new ForkJoinPool(
//            Runtime.getRuntime().availableProcessors(),
//            pool -> {
//                final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
//                worker.setName("ChunkTickPoolWorker-" + worker.getPoolIndex());
//                return worker;
//            },
//            (t, e) -> mc.getLogger().crash(new CrashReport("ChunkWorker " + t.getName() + " crashed with", e)),
//            false
//    );

    @Override
    protected void tickChunks(float partialTicks, @NotNull Collection<ChunkBase> chunks) {
//        try {
//            this.chunkTickPool.submit(() -> {
//                Stream<ChunkBase> stream = chunks.parallelStream();
//                stream.forEach(c -> c.tick(partialTicks));
//            }).get();
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
        for (ChunkBase chunk : chunks) {
            chunk.tick(partialTicks);
        }
    }

    @NotNull
    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
    }

//    /**
//     * Rebuilds all chunks partially rendering the block at the specified position
//     *
//     * @param x            blockX coordinate
//     * @param y            blockY coordinate
//     * @param z            blockZ coordinate
//     * @param highPriority state whether the chunks should be the first to be built on the ChunkRebuilder
//     */
//    public void rebuildChunksFor(int x, int y, int z, boolean highPriority) {
//        ClientChunk chunk = getChunkForPosition(x, z);
//        Objects.requireNonNull(chunk, "Chunk not found for x: " + x + ", z: " + z);
//        chunk.rebuildFor(x, y, z);
//    }

    @Override
    @ThreadSafe
    public void spawnEntityInWorld(@NotNull Entity entity) {
        super.spawnEntityInWorld(entity);
    }

    @Override
    public void stopAsyncWork() {
        super.stopAsyncWork();
        this.getParticleEngine().stopAsyncWork();
        this.chunkMeshRebuilder.stopAsyncWork();
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
        ((ClientMinecraft) this.mc).nettyClient.sendPacket(new ClientChunkLoadingDistanceChangePacket(null, chunkLoadingDistance));
    }

    /**
     * Sets the chunkLoadingDistance for the world object without sending a change packet to the server
     *
     * @param chunkLoadingDistance the new chunk loading distance packet
     */
    public void modifyChunkLoadingDistance(int chunkLoadingDistance) {
        super.setChunkLoadingDistance(chunkLoadingDistance);
    }

    @NotNull
    @Override
    public ClientWorldChunkHandler getWorldChunkHandler() {
        return (ClientWorldChunkHandler) super.getWorldChunkHandler();
    }

    @Nullable
    @Override
    public ClientChunk getChunkFor(@NotNull Entity entity) {
        return (ClientChunk) super.getChunkFor(entity);
    }

    @Nullable
    @Override
    public ClientChunk getChunkForPosition(float x, float z) {
        return (ClientChunk) super.getChunkForPosition(x, z);
    }

    @Nullable
    @Override
    public ClientChunk getChunkAtOrigin(int originX, int originZ) {
        return (ClientChunk) super.getChunkAtOrigin(originX, originZ);
    }

    @NotNull
    public ChunkMeshRebuilder getChunkMeshRebuilder() {
        return chunkMeshRebuilder;
    }

    /**
     * The chunk rendering manager that prepares a iteration over renderables
     */
    @NotNull
    public RenderManager getRenderManager() {
        return renderManager;
    }

    /**
     * The viewer of the single player world that is the player playing the game on this computer
     */
    @NotNull
    public EntityPlayerSP getViewer() {
        return viewer;
    }

    /**
     * Object used for spawning particle structures into the world.
     */
    public ParticleEngine getParticleEngine() {
        return particleEngine;
    }

    public void setParticleEngine(ParticleEngine particleEngine) {
        this.particleEngine = particleEngine;
    }

    public interface OnClientChunkCreationListener extends OnChunkCreationListener {
    }
}
