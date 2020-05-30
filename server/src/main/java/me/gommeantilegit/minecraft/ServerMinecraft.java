package me.gommeantilegit.minecraft;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.craftingboard.WebUI;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.data.ServerDataProvider;
import me.gommeantilegit.minecraft.server.config.ServerConfiguration;
import me.gommeantilegit.minecraft.server.console.ConsoleReader;
import me.gommeantilegit.minecraft.server.netty.NettyServer;
import me.gommeantilegit.minecraft.timer.tick.MinecraftThread;
import me.gommeantilegit.minecraft.utils.Clock;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.options.WorldGenerationOptions;
import me.gommeantilegit.minecraft.world.saveformat.ChunkFragmenter;
import me.gommeantilegit.minecraft.world.saveformat.WorldLoader;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static me.gommeantilegit.minecraft.Side.SERVER;

public class ServerMinecraft extends AbstractMinecraft {

    /**
     * Name of the server world
     */
    @NotNull
    private static final String SERVER_WORLD_NAME = "world";

//    /**
//     * File extension for world files
//     */
//    @NotNull
//    private static final String WORLD_FILE_EXT = ".lvl";

    /**
     * Server world instance
     */
    public ServerWorld theWorld;

    /**
     * Netty connection
     */
    public NettyServer nettyServer;

    /**
     * Console reader thread processing console input commands
     */
    private ConsoleReader consoleReader;

    /**
     * The server configuration wrapper to manage configurable properties
     */
    public ServerConfiguration configuration;

    /**
     * Directory where worlds are stored
     */
    @NotNull
    private final File worldsDirectory = new File("./worlds/");

    /**
     * The web-ui of the minecraft server
     */
    private WebUI webUI;

    /**
     * The thread the minecraft tick is performed on
     */
    private MinecraftThread minecraftThread;

    public ServerMinecraft() {
        super(SERVER);
        // Shutdown hook for unplanned exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!ServerMinecraft.this.isShutdownPlanned()) {
                CompletableFuture<Void> result = ServerMinecraft.this.saveWorld();
                try {
                    result.get(); // Waiting for world to have saved
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                try {
                    saveLog("unexpected_shutdown");
                } catch (IOException e) {
                    getLogger().exception("Failed to save log on unplanned shutdown!", e);
                }
            }
        }));
    }

    /**
     * Saves the log of {@link #getLogger()} in a new log file
     *
     * @param applicationExitState exit state of the application (eg. unexpected_shutdown, crash, stop)
     * @throws IOException if io access fails while saving
     */
    private void saveLog(@NotNull String applicationExitState) throws IOException {
        this.getLogger().save(applicationExitState);
    }

    @NotNull
    protected MinecraftThread createMinecraftThread() {
        return new MinecraftThread(this, configuration.getIdleTicks()) {

            /**
             * Timer instance to time world autosaving
             */
            @NotNull
            private final Clock worldAutoSaverTimer = new Clock(false);

            /**
             * Five minutes in milliseconds
             */
            public static final int AUTO_SAVE_TIME_LIMIT = 1000 * 60 * 5;

            @Override
            public void onUpdate() {
                super.onUpdate();
                if (worldAutoSaverTimer.getTimePassed() > AUTO_SAVE_TIME_LIMIT) {
                    getLogger().info("Autosaving world...");
                    saveWorld();

                    worldAutoSaverTimer.reset();
                }
            }

            @Override
            protected void onInterrupted() {
                super.onInterrupted();
                getLogger().info("Minecraft Thread interrupted");
            }
        };
    }

    @Override
    public void loadGame() {
        super.loadGame();

        this.getLogger().info("Loading Server...");

        // Server properties
        try {
            this.configuration = ServerConfiguration.loadServerConfiguration(this);
        } catch (IOException e) {
            getLogger().crash("Failed to log server configuration!", e);
        }

        // Blocks init
        this.setBlocks(new Blocks(this));
        this.getBlocks().init();

        this.chunkFragmenter = new ChunkFragmenter(this);

        {
            File worldDir = new File(this.worldsDirectory, SERVER_WORLD_NAME);
            if (!worldDir.exists()) {
                this.getLogger().info("Creating world...");
                WorldGenerator worldGenerator = new WorldGenerator(this, new WorldGenerationOptions("Glacier".hashCode(), WorldGenerationOptions.WorldType.OVERWORLD, false));
                this.theWorld = new ServerWorld(this, worldGenerator, worldDir, this.getBlocks().getGlobalPalette());
                this.theWorld.setChunkLoadingDistance(configuration.getMaxChunkLoadingDistance()); // Setting chunk loading distance to configuration max
                this.getLogger().info("Preparing spawn area...");
                // Prepare spawn area
                {
                    ExecutorService generationSwarm = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), r -> {
                        Thread thread = new Thread(r, "SpawnAreaGeneration-Worker");
                        thread.setPriority(Thread.NORM_PRIORITY);
                        thread.setDaemon(true);
                        return thread;
                    });
                    AtomicInteger i = new AtomicInteger();
                    int maxDistance = ChunkBase.CHUNK_SIZE * 15;
                    int expectedNumChunks = (maxDistance * 2 * maxDistance * 2) / (ChunkBase.CHUNK_SIZE * ChunkBase.CHUNK_SIZE); // approximation
                    {
                        long start = System.nanoTime();
                        List<Future<ChunkBase>> futures = this.theWorld.getChunkCreator().generateChunksAroundPositionAsync(generationSwarm, new Vector3(0, 0, 0), maxDistance, created -> {
                            if (i.get() % 10 == 0) {
                                float percent = i.get() / (float) expectedNumChunks;
                                percent = Math.min(percent, 1);
                                this.getLogger().info("Preparing spawn area: " + (int) (percent * 100) + "%");
                            }
                            i.incrementAndGet();
                        });
                        for (Future<ChunkBase> future : futures) {
                            try {
                                future.get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                        long end = System.nanoTime();
                        this.getLogger().info("Spawn Area Generation took: " + (end - start) / 1E6f + " ms");
                        generationSwarm.shutdownNow();
                    }
                    this.theWorld.defineSpawnPosition();
                }
                this.getLogger().info("Spawn area prepared");
            } else {
                this.getLogger().info("Loading world...");
                WorldLoader worldLoader = new WorldLoader(new File(worldDir, SERVER_WORLD_NAME), this);
                try {
                    this.theWorld = worldLoader.loadWorld();
                } catch (Exception e) {
                    this.getLogger().crash("Loading of world failed. (NBT Reading)", e);
                }
                this.getLogger().info("World loading complete.");
            }
        }

        // Netty server thread init
        this.nettyServer = new NettyServer(STD_PORT, this);
        this.nettyServer.start();

        // Starting the minecraft thread
        this.minecraftThread = createMinecraftThread();
        this.minecraftThread.start();

        // Console command reader init
        this.consoleReader = new ConsoleReader(this);
        this.consoleReader.start();

        this.getLogger().info("Starting WebUI...");
        this.webUI = new WebUI(new ServerDataProvider(this), this.configuration.getWebUIPort()); // starts a thread
        this.getLogger().info("WebUI started on " + this.webUI.getURL());

        this.getLogger().info("Server loaded!");
    }

    /**
     * Saves the server world asynchronously
     */
    @NotNull
    public CompletableFuture<Void> saveWorld() {
        return CompletableFuture.runAsync(() -> {
            try {
                getLogger().info("Saving world...");
                this.theWorld.save();
                getLogger().info("Saved world!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void shutdown() {
        stop();
    }

    /**
     * Stops the server.
     * Server also saves world.
     */
    public void stop() {
        this.getLogger().planShutdown();
        try {
            CompletableFuture<Void> result = this.saveWorld();
            result.get(); // Waiting for world to have saved
        } catch (Throwable e) {
            this.getLogger().exceptionFatal("Unexpected Throwable caught", e);
        }
        getLogger().info("Stopping Async World Threads...");
        this.theWorld.stopAsyncWork();
        try {
            try {
                this.saveLog("stop");
            } catch (IOException e) {
                this.getLogger().exception("Failed to save log!", e);
            }
        } catch (Throwable e) {
            this.getLogger().exceptionFatal("Unexpected Throwable caught", e);
        }
        this.minecraftThread.interrupt();
        this.nettyServer.interrupt();
        this.consoleReader.interrupt();
    }

    @Override
    public boolean isRunning() {
        return this.minecraftThread != null && this.minecraftThread.getState() == Thread.State.NEW || (this.minecraftThread != null && this.minecraftThread.isAlive());
    }

    @Override
    public boolean isCallingFromMinecraftThread() {
        return Thread.currentThread() == this.minecraftThread;
    }

    @Override
    public void tick(float partialTicks) {
        this.theWorld.tick(partialTicks);
    }

    public boolean isShutdownPlanned() {
        return this.getLogger().isShutdownPlanned();
    }

    @NotNull
    public MinecraftThread getMinecraftThread() {
        return minecraftThread;
    }
}
