package me.gommeantilegit.minecraft.world;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.state.palette.IBlockStatePalette;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.logging.crash.CrashReport;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.change.BlockStateSemaphoreBase;
import me.gommeantilegit.minecraft.world.chunk.creator.ServerChunkCreator;
import me.gommeantilegit.minecraft.world.chunk.loader.ServerChunkLoader;
import me.gommeantilegit.minecraft.world.chunk.world.ServerWorldChunkHandler;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.saveformat.WorldSaver;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.Stream;

import static me.gommeantilegit.minecraft.Side.SERVER;

@SideOnly(side = SERVER)
public class ServerWorld extends WorldBase {

    /**
     * ServerWorld Generator
     */
    @NotNull
    private final WorldGenerator worldGenerator;

    /**
     * The position where players spawn
     */
    @NotNull
    private Vector3 spawnPoint = new Vector3(0, 255, 0);

    /**
     * Saves the world
     */
    @NotNull
    private final WorldSaver worldSaver;

    /**
     * ForkJoin for chunk ticking
     */
    @NotNull
    private final ForkJoinPool chunkTickPool = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            pool -> {
                final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                worker.setName("ChunkTickPoolWorker-" + worker.getPoolIndex());
                worker.setDaemon(true);
                return worker;
            },
            (t, e) -> mc.getLogger().crash(new CrashReport("ChunkWorker " + t.getName() + " crashed with", e)),
            false
    );


    /**
     * Default world constructor
     *
     * @param mc                Minecraft instance
     * @param worldGenerator    world generator for world generation
     * @param worldDirectory    the directory to save the minecraft world into
     * @param height            block height of the world
     * @param blockStatePalette the block state palette used for block storage
     */
    public ServerWorld(@NotNull ServerMinecraft mc, @NotNull WorldGenerator worldGenerator, @NotNull File worldDirectory, int height, @NotNull IBlockStatePalette blockStatePalette) {
        super(mc, height, blockStatePalette, new BlockStateSemaphoreBase());
        this.worldGenerator = worldGenerator;
        this.chunkCreator = new ServerChunkCreator(this);
        this.worldChunkHandler = new ServerWorldChunkHandler(); // after #chunkCreator
        this.chunkLoader = new ServerChunkLoader(this, mc);
        this.worldSaver = new WorldSaver(this, worldDirectory);
    }

    /**
     * @see #ServerWorld(ServerMinecraft, WorldGenerator, File, int, IBlockStatePalette)
     */
    public ServerWorld(@NotNull ServerMinecraft serverMinecraft, @NotNull WorldGenerator worldGenerator, @NotNull File worldDirectory, @NotNull IBlockStatePalette blockStatePalette) {
        this(serverMinecraft, worldGenerator, worldDirectory, STANDARD_WORLD_HEIGHT, blockStatePalette);
    }

    @Override
    public void spawnEntityInWorld(@NotNull Entity entity) {
        super.spawnEntityInWorld(entity);
    }

    @NotNull
    @Override
    public ServerWorldChunkHandler getWorldChunkHandler() {
        return (ServerWorldChunkHandler) super.getWorldChunkHandler();
    }

    @NotNull
    @Override
    public ServerChunkCreator getChunkCreator() {
        return (ServerChunkCreator) super.getChunkCreator();
    }

    @Override
    public void tick(float partialTicks) {
        super.tick(partialTicks);
    }

    @Override
    protected void tickChunks(float partialTicks, @NotNull Collection<ChunkBase> chunks) {
        this.chunkTickPool.submit(() -> {
            Stream<ChunkBase> stream = chunks.parallelStream();
            stream.forEach(c -> c.tick(partialTicks));
        }).join();
    }

    /**
     * @return the block height of the world
     */
    public int getHeight() {
        return super.getHeight();
    }

    /**
     * Chooses the spawn position where players will spawn
     */
    public void defineSpawnPosition() {
        for (int y = super.getHeight(); y >= 0; y--) {
            if (getBlockState(0, y, 0) != null) {
                setSpawnPoint(new Vector3(0.5f, y + 3.5f, 0.5f));
                break;
            }
        }
    }

    public void setSpawnPoint(@NotNull Vector3 spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    /**
     * Saves the world
     */
    public void save() {
        this.worldSaver.saveAllChunks();
    }

    @NotNull
    public WorldGenerator getWorldGenerator() {
        return worldGenerator;
    }

    @NotNull
    public Vector3 getSpawnPoint() {
        return spawnPoint;
    }
}
