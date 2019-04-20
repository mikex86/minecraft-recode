package me.gommeantilegit.minecraft.world;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.ServerBlock;
import me.gommeantilegit.minecraft.block.ServerBlocks;
import me.gommeantilegit.minecraft.block.state.ServerBlockState;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.world.block.change.ServerWorldBlockChanger;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
import me.gommeantilegit.minecraft.world.chunk.creator.OnChunkCreationListener;
import me.gommeantilegit.minecraft.world.chunk.creator.ServerChunkCreator;
import me.gommeantilegit.minecraft.world.chunk.loader.ServerChunkLoader;
import me.gommeantilegit.minecraft.world.chunk.world.ServerWorldChunkHandler;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.gommeantilegit.minecraft.Side.SERVER;

@SideOnly(side = SERVER)
public class ServerWorld extends WorldBase<ServerChunkLoader, ServerBlocks, ServerMinecraft, ServerWorld, ServerBlockState, ServerChunk, ServerWorldChunkHandler, ServerBlock, ServerWorldBlockChanger> {

    /**
     * ServerWorld Generator
     */
    @NotNull
    private final WorldGenerator worldGenerator;

    /**
     * Default {@link OnServerChunkCreationListener} invoked on chunk creation.
     *
     * @see OnServerChunkCreationListener
     * @see OnServerChunkCreationListener#onChunkCreated(ChunkBase)
     */
    @NotNull
    private List<OnServerChunkCreationListener> onChunkCreationListeners = new ArrayList<>();

    /**
     * The position where players spawn
     */
    @NotNull
    private Vector3 spawnPoint = new Vector3(0, 255, 0);

    /**
     * The listener that decides whether terrain generation should be invoked for a specified chunk
     */
    @NotNull
    private InvokeTerraionGenerationDecider invokeTerraionGenerationDecider = chunk -> true;

    /**
     * Default world constructor
     *
     * @param mc             Minecraft instance
     * @param worldGenerator world generator for world generation
     * @param height         block height of the world
     */
    public ServerWorld(@NotNull ServerMinecraft mc, @NotNull WorldGenerator worldGenerator, int height) {
        super(mc, height);
        this.worldThread = new Thread(this, "ServerWorld-thread");
        this.worldChunkHandler = new ServerWorldChunkHandler(mc);
        this.worldGenerator = worldGenerator;
        this.chunkCreator = new ServerChunkCreator(this);
        this.blockChanger = new ServerWorldBlockChanger(this);
        this.chunkLoader = new ServerChunkLoader(this);

        // MUST BE THE LAST THING TO PERFORM IN WORLD CONSTRUCTOR
        this.worldThread.setDaemon(true);
        this.worldThread.start();
    }

    /**
     * @see #ServerWorld(ServerMinecraft, WorldGenerator, int)
     */
    public ServerWorld(@NotNull ServerMinecraft serverMinecraft, WorldGenerator worldGenerator) {
        this(serverMinecraft, worldGenerator, STANDARD_WORLD_HEIGHT);
    }

    /**
     * ServerWorld onTick update
     */
    @Override
    public void tick(float partialTicks) {
        if (!isLoaded)
            return;

        ArrayList<ServerChunk> chunks = this.worldChunkHandler.getLoadedChunks();
        for (int i = 0; i < chunks.size(); i++) {
            ServerChunk chunk = chunks.get(i);
            chunk.tick(partialTicks);
        }
        super.tick(partialTicks);
    }

    @Override
    public void spawnEntityInWorld(Entity entity) {
        super.spawnEntityInWorld(entity);
        chunkCreator.submit(entity); // Submitting the entity to the chunk creator
    }

    /**
     * Invoked by the world thread
     */
    @Override
    public void onAsyncThread() {
        this.blockChanger.onAsyncThread();
        this.chunkCreator.onAsyncThread();
        this.chunkLoader.onAsyncThread();
    }

    /**
     * @return the block height of the world
     */
    public int getHeight() {
        return height;
    }

    /**
     * Chooses the spawn position where players will spawn
     */
    public void defineSpawnPosition() {
        for (int y = height; y >= 0; y--) {
            if (getBlockState(0, y, 0) != null) {
                setSpawnPoint(new Vector3(0.5f, y + 3.5f, 0.5f));
                break;
            }
        }
    }

    public void setSpawnPoint(@NotNull Vector3 spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public interface OnServerChunkCreationListener extends OnChunkCreationListener<ServerChunk> {
    }

    public interface InvokeTerraionGenerationDecider {
        /**
         * Method decides whether terrain generation for the given chunk should be invoked or not
         * @param chunk the chunk
         * @return true if terrain generation should be invoked for the chunk
         */
        boolean invokedTerrainGeneration(@NotNull ServerChunk chunk);
    }

    public ServerWorld addOnChunkCreationListener(@Nullable OnServerChunkCreationListener onChunkCreationListener) {
        this.onChunkCreationListeners.add(onChunkCreationListener);
        return this;
    }

    @NotNull
    public ServerWorldBlockChanger getBlockChanger() {
        return blockChanger;
    }

    @NotNull
    public WorldGenerator getWorldGenerator() {
        return worldGenerator;
    }

    @NotNull
    public ServerChunkLoader getChunkLoader() {
        return chunkLoader;
    }

    @NotNull
    public ServerWorldChunkHandler getWorldChunkHandler() {
        return worldChunkHandler;
    }

    @NotNull
    public List<OnServerChunkCreationListener> getOnChunkCreationListeners() {
        return onChunkCreationListeners;
    }

    public void setInvokeTerraionGenerationDecider(@NotNull InvokeTerraionGenerationDecider invokeTerraionGenerationDecider) {
        this.invokeTerraionGenerationDecider = invokeTerraionGenerationDecider;
    }

    @NotNull
    public InvokeTerraionGenerationDecider getInvokeTerraionGenerationDecider() {
        return invokeTerraionGenerationDecider;
    }

    @NotNull
    public Vector3 getSpawnPoint() {
        return spawnPoint;
    }
}
