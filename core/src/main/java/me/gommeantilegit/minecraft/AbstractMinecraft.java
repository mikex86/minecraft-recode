package me.gommeantilegit.minecraft;

import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.logging.LogLevel;
import me.gommeantilegit.minecraft.logging.Logger;
import me.gommeantilegit.minecraft.logging.crash.CrashReport;
import me.gommeantilegit.minecraft.logging.file.handler.LogFileHandler;
import me.gommeantilegit.minecraft.timer.Timer;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.logging.Hardware;
import me.gommeantilegit.minecraft.world.saveformat.ChunkFragmenter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;

public abstract class AbstractMinecraft implements Tickable {

    /**
     * Default server port constant
     */
    public static final int STD_PORT = 25565;

    /**
     * Side of this minecraft implementation
     */
    @NotNull
    private final Side side;

    /**
     * The id of the minecraft instance provided by the {@link MinecraftProvider}
     */
    private final long id;

    /**
     * Manages block instances
     */
    @Nullable
    private Blocks blocks;

    @NotNull
    private Timer timer = new Timer(20.0f);

    /**
     * Handler object for handling file names of log files
     */
    @NotNull
    protected final LogFileHandler logFileHandler;

    /**
     * The minecraft root logger instance
     */
    @NotNull
    private final Logger logger;

    /**
     * Minecraft version constant
     */
    @NotNull
    public final static String MINECRAFT_VERSION_STRING = "1.0.0";

    /**
     * The minecraft versions prefix like "Alpha" or "Beta"
     */
    @NotNull
    public static final String MINECRAFT_VERSION_PREFIX = "Alpha";

    /**
     * The minecraft versions prefix char like 'a' for alpha or 'b' for beta
     */
    public static final char MINECRAFT_VERSION_PREFIX_CHAR = 'a';

    protected ChunkFragmenter chunkFragmenter;

    protected AbstractMinecraft(@NotNull Side side) {
        Hardware.init();
        this.id = MinecraftProvider.createMinecraft(this);

        this.side = side;
        try {
            this.logFileHandler = new LogFileHandler(new File("./logs/")); // Initializing log file handler
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize LogFileHandler", e);
        }
        this.logger = new Logger(LogLevel.DEBUG, logFileHandler); // Initializing logger
        System.setOut(new PrintStream(new ByteArrayOutputStream()) {

            @Override
            public void print(String s) {
                super.print(s);
                getLogger().info("[STD_OUT]", s, true);
            }
        });
    }

    /**
     * Loads the game.
     * Super constructor must be called
     */
    protected void loadGame() {
    }

    /**
     * @return true if minecraft is running else false
     */
    public abstract boolean isRunning();

    /**
     * @return the tick timer of the minecraft instance
     */
    @NotNull
    public Timer getTimer() {
        return timer;
    }

    @NotNull
    public Side getSide() {
        return side;
    }

    @Override
    protected void finalize() throws Throwable {
        MinecraftProvider.removeMinecraft(getId());
        super.finalize();
    }

    static {
        CrashReport.init();
    }

    /**
     * @return true, if this method was executed on the minecraft thread else false.
     */
    public abstract boolean isCallingFromMinecraftThread();

    /**
     * Shuts down minecraft
     */
    public abstract void shutdown();

    /**
     * Blocks instance
     */
    @NotNull
    public Blocks getBlocks() {
        return Objects.requireNonNull(blocks, "Blocks not yet initialized!");
    }

    public void setBlocks(@NotNull Blocks blocks) {
        this.blocks = blocks;
    }

    /**
     * ID of the minecraft instance in the minecraft provider
     */
    public long getId() {
        return id;
    }

    /**
     * Default logger instance of the server
     */
    @NotNull
    public Logger getLogger() {
        return logger;
    }

    public void setTimer(@NotNull Timer timer) {
        this.timer = timer;
    }

    /**
     * Called to update the tick game timer. This is called at rates not to be made any kind of assumption about
     */
    public void onUpdate() {
        //Updating timer and ticks
        this.timer.advanceTime();
        for (int i = 0; i < this.timer.ticks; i++) {
            this.tick(this.timer.partialTicks);
            this.timer.tick(this.timer.partialTicks);
        }
    }

    @NotNull
    public ChunkFragmenter getChunkFragmenter() {
        return Objects.requireNonNull(chunkFragmenter, "Chunk Fragmenter not yet initialized");
    }
}

