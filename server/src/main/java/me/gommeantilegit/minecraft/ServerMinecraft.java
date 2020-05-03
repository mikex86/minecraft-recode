package me.gommeantilegit.minecraft;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.block.state.storage.BlockStateStorage;
import me.gommeantilegit.minecraft.server.config.ServerConfiguration;
import me.gommeantilegit.minecraft.server.console.ConsoleReader;
import me.gommeantilegit.minecraft.server.netty.NettyServer;
import me.gommeantilegit.minecraft.timer.tick.MinecraftThread;
import me.gommeantilegit.minecraft.utils.Clock;
import me.gommeantilegit.minecraft.utils.async.AsyncResult;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.options.WorldGenerationOptions;
import me.gommeantilegit.minecraft.world.saveformat.WorldLoader;
import me.gommeantilegit.minecraft.world.saveformat.WorldSaver;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipOutputStream;

import static me.gommeantilegit.minecraft.Side.SERVER;

public class ServerMinecraft extends AbstractMinecraft {

    /**
     * Name of the server world
     */
    @NotNull
    private static final String SERVER_WORLD_NAME = "world";

    /**
     * File extension for world files
     */
    @NotNull
    private static final String WORLD_FILE_EXT = ".lvl";

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
    @Override
    protected MinecraftThread createMinecraftThread() {
        return new MinecraftThread(this) {

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
            this.configuration = new ServerConfiguration(this);
        } catch (IOException e) {
            getLogger().crash("Failed to log server configuration!", e);
        }

        // Blocks init
        this.setBlocks(new Blocks(this));
        this.getBlocks().init();
        BlockStateStorage.initPalette(this.getBlocks());

        {
            File worldFile = new File(this.worldsDirectory, SERVER_WORLD_NAME + WORLD_FILE_EXT);
            if (!worldFile.exists()) {
                this.getLogger().info("Creating world...");
                WorldGenerator worldGenerator = new WorldGenerator("Glacier".hashCode(), WorldGenerator.WorldType.OVERWORLD, this, new WorldGenerationOptions(false));
                this.theWorld = new ServerWorld(this, worldGenerator);
                this.theWorld.setChunkLoadingDistance(configuration.getMaxChunkLoadingDistance() + 16); // Setting chunk loading distance to configuration max + 16 blocks
                this.getLogger().info("Preparing spawn area...");
                this.theWorld.getChunkCreator().generateChunksAroundPosition(new Vector3(0, 0, 0), 16);
                this.theWorld.defineSpawnPosition();
                this.getLogger().info("Spawn area prepared");
            } else {
                try {
                    this.getLogger().info("Loading world...");
                    WorldLoader worldLoader = new WorldLoader(new File(this.worldsDirectory, SERVER_WORLD_NAME + WORLD_FILE_EXT), this);
                    try {
                        this.theWorld = worldLoader.loadWorld();
                    } catch (Exception e) {
                        this.getLogger().crash("Loading of world failed. (NBT Reading)", e);
                    }
                    this.getLogger().info("World loading complete.");
                } catch (IOException e) {
                    this.getLogger().crash("Level file not found!", e);
                }
            }
        }

        // Netty server thread init
        this.nettyServer = new NettyServer(STD_PORT, this);
        this.nettyServer.start();

        // Console command reader init
        this.consoleReader = new ConsoleReader(this);
        this.consoleReader.start();

        // Starting the minecraft thread
        this.getMinecraftThread().startMinecraftGameLogic(); // Enabling the game logic before the thread is even started
        this.getMinecraftThread().start();

        this.getLogger().info("Server loaded!");

        try {
            // Waiting for the minecraft thread to terminate
            this.getMinecraftThread().join();
        } catch (InterruptedException e) {
            getLogger().info("Minecraft Thread interrupted");
        }
    }

    /**
     * Saves the server world asynchronously
     */
    @NotNull
    public CompletableFuture<Void> saveWorld() {
        return CompletableFuture.runAsync(() -> {
            try {
                getLogger().info("Saving world...");
                WorldSaver worldSaver = new WorldSaver(theWorld);
                worldsDirectory.mkdirs();
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                ZipOutputStream zipOut = new ZipOutputStream(bytesOut);
                worldSaver.save(zipOut);

                FileOutputStream fileOutputStream = new FileOutputStream(new File(worldsDirectory, SERVER_WORLD_NAME + WORLD_FILE_EXT));
                fileOutputStream.write(bytesOut.toByteArray());
                fileOutputStream.close();

                getLogger().info("Invoking Garbage collection after saving world...");
                System.gc();
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
        this.getMinecraftThread().interrupt();
        this.nettyServer.interrupt();
        this.consoleReader.interrupt();
    }

    @Override
    public boolean isRunning() {
        return this.getMinecraftThread().getState() == Thread.State.NEW || this.getMinecraftThread().isAlive();
    }

    @Override
    public void tick(float partialTicks) {
        this.theWorld.tick(partialTicks);
    }

    public boolean isShutdownPlanned() {
        return this.getLogger().isShutdownPlanned();
    }
}
