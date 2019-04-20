package me.gommeantilegit.minecraft;

import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.logging.LogLevel;
import me.gommeantilegit.minecraft.logging.Logger;
import me.gommeantilegit.minecraft.logging.crash.CrashReport;
import me.gommeantilegit.minecraft.logging.file.handler.LogFileHandler;
import me.gommeantilegit.minecraft.timer.Timer;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.timer.tick.MinecraftThread;
import me.gommeantilegit.minecraft.utils.Hardware;
import me.gommeantilegit.minecraft.utils.async.AsyncExecutor;
import me.gommeantilegit.minecraft.world.saveformat.ChunkSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public abstract class AbstractMinecraft implements Tickable {

    /**
     * Default server port constant
     */
    public static final int STD_PORT = 25565;

    /**
     * Side of this minecraft implementation
     */
    @NotNull
    final Side side;

    /**
     * Minecraft thread instance
     */
    @NotNull
    public final MinecraftThread minecraftThread = createMinecraftThread();

    /**
     * ID of the minecraft instance in the minecraft provider
     */
    public final long id;

    @NotNull
    public final ChunkSerializer chunkSerializer;

    /**
     * @return the minecraft thread to be used as initial value for {@link #minecraftThread}
     */
    @NotNull
    protected abstract MinecraftThread createMinecraftThread();

    /**
     * Blocks instance
     */
    public Blocks blocks;

    /**
     * Timer instance for timing game logic.
     */
    @NotNull
    public Timer timer = new Timer(20.0f);

    /**
     * Handler object for handling file names of log files
     */
    @NotNull
    protected final LogFileHandler logFileHandler;

    /**
     * Default logger instance of the server
     */
    @NotNull
    public final Logger logger;

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

    /**
     * Asynchronous executor instance
     */
    public AsyncExecutor asyncExecutor;

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
                logger.info("[STD_OUT]", s, true);
            }
        });
        chunkSerializer = new ChunkSerializer(this);
    }

    /**
     * Loads the game.
     * Super constructor must be called
     */
    protected void loadGame(){
        this.asyncExecutor = new AsyncExecutor(16);
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
        MinecraftProvider.removeMinecraft(id);
        super.finalize();
    }

    static {
        CrashReport.init();
    }

    /**
     * @return true, if this method was executed on the minecraft thread else false.
     */
    public boolean isCallingFromMinecraftThread() {
        return Thread.currentThread() == this.minecraftThread;
    }

    /**
     * Shuts down minecraft
     */
    public abstract void shutdown();
}

