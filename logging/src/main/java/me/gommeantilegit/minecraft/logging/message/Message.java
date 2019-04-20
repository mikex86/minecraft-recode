package me.gommeantilegit.minecraft.logging.message;

import me.gommeantilegit.minecraft.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Object representing a message logged
 */
public class Message {

    /**
     * The message string to be logged
     */
    @NotNull
    private final String message;

    /**
     * Log level of the message
     */
    @NotNull
    private final LogLevel logLevel;

    /**
     * The Thread that the message was logged from
     */
    @NotNull
    private final Thread logThread;

    /**
     * The date instance parent to the log time of the message
     */
    @NotNull
    private final Date date;

    /**
     * Date format
     */
    @NotNull
    public static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    /**
     * Additional prefix of the message. Ignored if null
     */
    @Nullable
    private final String prefix;

    public Message(@NotNull String message, @NotNull LogLevel logLevel, @NotNull Thread logThread) {
        this.message = message;
        this.logLevel = logLevel;
        this.logThread = logThread;
        this.date = new Date();
        this.prefix = null;
    }

    public Message(@Nullable String prefix, @NotNull String message, @NotNull LogLevel logLevel, @NotNull Thread logThread) {
        this.message = message;
        this.logLevel = logLevel;
        this.logThread = logThread;
        this.date = new Date();
        this.prefix = prefix;
    }

    /**
     * Prints out the message to the print stream
     *
     * @param printStream the print stream that the message should be print to
     */
    public void printOut(@NotNull PrintStream printStream) {
        printStream.println(toString());
    }

    @Override
    public String toString() {
        return "[" + SDF.format(date) + "][" + logLevel.name() + "][" + logThread.getName() + ":" + logThread.getState().name() + "]" + (prefix == null ? "" : prefix) + " >> " + message;
    }

    @NotNull
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    @NotNull
    public Thread getLogThread() {
        return logThread;
    }

    @NotNull
    public Date getDate() {
        return date;
    }
}
