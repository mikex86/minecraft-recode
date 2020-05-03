package me.gommeantilegit.minecraft.world;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.logging.crash.CrashReport;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.creator.ServerChunkCreator;
import me.gommeantilegit.minecraft.world.chunk.loader.ServerChunkLoader;
import me.gommeantilegit.minecraft.world.chunk.world.ServerWorldChunkHandler;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
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
     * The listener that decides whether terrain generation should be invoked for a specified chunk
     */
    @NotNull
    private InvokeTerrainGenerationDecider invokeTerrainGenerationDecider = (world, chunk) -> true;

    /**
     * ForkJoin for chunk ticking
     */
    @NotNull
    private ForkJoinPool chunkTickPool = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            pool -> {
                final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                worker.setName("ChunkTickPoolWorker-" + worker.getPoolIndex());
                return worker;
            },
            (t, e) -> mc.getLogger().crash(new CrashReport("ChunkWorker " + t.getName() + " crashed with", e)),
            false
    );


    /**
     * Default world constructor
     *
     * @param mc             Minecraft instance
     * @param worldGenerator world generator for world generation
     * @param height         block height of the world
     */
    public ServerWorld(@NotNull ServerMinecraft mc, @NotNull WorldGenerator worldGenerator, int height) {
        super(mc, height);
        this.worldGenerator = worldGenerator;
        this.chunkCreator = new ServerChunkCreator(this);
        this.worldChunkHandler = new ServerWorldChunkHandler(chunkCreator); // after #chunkCreator
        this.chunkLoader = new ServerChunkLoader(this, mc);
    }

    /**
     * @see #ServerWorld(ServerMinecraft, WorldGenerator, int)
     */
    public ServerWorld(@NotNull ServerMinecraft serverMinecraft, WorldGenerator worldGenerator) {
        this(serverMinecraft, worldGenerator, STANDARD_WORLD_HEIGHT);
    }

    @Override
    public void spawnEntityInWorld(Entity entity) {
        super.spawnEntityInWorld(entity);
    }

    @Override
    public void tick(float partialTicks) {
        super.tick(partialTicks);
    }

    @Override
    protected void tickChunks(float partialTicks, @NotNull Collection<ChunkBase> chunks) {
        try {
            this.chunkTickPool.submit(() -> {
                Stream<ChunkBase> stream = chunks.parallelStream();
                stream.forEach(c -> c.tick(partialTicks));
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
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

    public interface InvokeTerrainGenerationDecider {

        /**
         * Method decides whether terrain generation for the given chunk should be invoked or not
         *
         * @param world the world
         * @param chunk the chunk
         * @return true if terrain generation should be invoked for the chunk
         */
        boolean shouldInvokeTerrainGeneration(@NotNull WorldBase world, @NotNull ChunkBase chunk);
    }

    public void setInvokeTerrainGenerationDecider(@NotNull InvokeTerrainGenerationDecider invokeTerrainGenerationDecider) {
        this.invokeTerrainGenerationDecider = invokeTerrainGenerationDecider;
    }

    @NotNull
    public InvokeTerrainGenerationDecider getInvokeTerrainGenerationDecider() {
        return invokeTerrainGenerationDecider;
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
