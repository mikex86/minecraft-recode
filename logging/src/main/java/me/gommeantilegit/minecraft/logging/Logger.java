package me.gommeantilegit.minecraft.logging;

import me.gommeantilegit.minecraft.logging.crash.CrashReport;
import me.gommeantilegit.minecraft.logging.file.handler.LogFileHandler;
import me.gommeantilegit.minecraft.logging.message.CrashReportMessage;
import me.gommeantilegit.minecraft.logging.message.ExceptionMessage;
import me.gommeantilegit.minecraft.logging.message.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;

/**
 * Logger object capable of logging messages
 */
public class Logger {

    /**
     * Log-Level
     */
    @NotNull
    private final LogLevel logLevel;

    /**
     * Stores all logged messages
     */
    @NotNull
    private final ArrayList<Message> messages = new ArrayList<>();

    /**
     * Log file handler instance
     */
    @NotNull
    private final LogFileHandler logFileHandler;

    /**
     * State if a shutdown of the application is planned (set to true on invocation of {@link #planShutdown()})
     * If this is true, it means that the shut down process has already been started
     */
    private boolean shutdownPlanned = false;

    public Logger(@NotNull LogLevel logLevel, @NotNull LogFileHandler logFileHandler) {
        this.logLevel = logLevel;
        this.logFileHandler = logFileHandler;
    }

    /**
     * Logs the given message
     *
     * @param prefix   the prefix of the message
     * @param message  the message to be logged
     * @param logLevel log level of the message
     * @param print    sate whether or not the message should be print to the console. if false -> message is still logged and thus saved into the log file
     */
    public void log(@Nullable String prefix, @NotNull String message, @NotNull LogLevel logLevel, boolean print) {
        Message msg = new Message(prefix, message, logLevel, Thread.currentThread());
        this.messages.add(msg);
        if (print)
            if (logLevel.getLogLevel() >= this.logLevel.getLogLevel()) {
                msg.printOut(msg.getLogLevel().getPrintStream());
            }
    }

    /**
     * Logs the given message
     *
     * @param message  the message to be logged
     * @param logLevel log level of the message
     * @param print    sate whether or not the message should be print to the console. if false -> message is still logged and thus saved into the log file
     */
    public void log(@NotNull String message, @NotNull LogLevel logLevel, boolean print) {
        log(null, message, logLevel, print);
    }

    /**
     * Logs the given message
     *
     * @param message  the message to be logged
     * @param logLevel log level of the message
     */
    public void log(@NotNull String message, @NotNull LogLevel logLevel) {
        log(message, logLevel, true);
    }

    /**
     * Logs the given message with {@link LogLevel#INFO}
     *
     * @param message the given message
     */
    public void info(@NotNull String message) {
        log(message, LogLevel.INFO);
    }


    /**
     * Logs the given message with {@link LogLevel#INFO}
     *
     * @param message the given message
     * @param print   sate whether or not the message should be print to the console. if false -> message is still logged and thus saved into the log file
     */
    public void info(@NotNull String message, boolean print) {
        log(message, LogLevel.INFO, print);
    }

    /**
     * Logs the given message with {@link LogLevel#INFO}
     *
     * @param prefix  additional prefix for the log message
     * @param message the given message
     * @param print   sate whether or not the message should be print to the console. if false -> message is still logged and thus saved into the log file
     */
    public void info(@NotNull String prefix, @NotNull String message, boolean print) {
        log(prefix, message, LogLevel.INFO, print);
    }

    /**
     * Logs the given message with {@link LogLevel#ERROR}
     *
     * @param prefix  additional prefix for the log message
     * @param message the given message
     * @param print   sate whether or not the message should be print to the console. if false -> message is still logged and thus saved into the log file
     */
    public void err(@NotNull String prefix, @NotNull String message, boolean print) {
        log(prefix, message, LogLevel.ERROR, print);
    }

    /**
     * Logs the given message with {@link LogLevel#DEBUG}
     *
     * @param message the given message
     */
    public void debug(@NotNull String message) {
        log(message, LogLevel.DEBUG);
    }

    /**
     * Logs the given message with {@link LogLevel#WARNING}
     *
     * @param message the given message
     */
    public void warn(@NotNull String message) {
        log(message, LogLevel.WARNING);
    }

    /**
     * Logs the given message with {@link LogLevel#ERROR}
     *
     * @param message the given message
     */
    public void err(@NotNull String message) {
        log(message, LogLevel.ERROR);
    }

    /**
     * Logs the given message with {@link LogLevel#FATAL}
     *
     * @param message the given message
     */
    public void fatal(@NotNull String message) {
        log(message, LogLevel.FATAL);
    }

    /**
     * Logs the given throwable as {@link ExceptionMessage}
     *
     * @param message   additional comment on the throwable
     * @param throwable the exception to be reported
     * @param logLevel  the logLevel of the exception
     */
    public void exception(@NotNull String message, @NotNull Throwable throwable, @NotNull LogLevel logLevel) {
        ExceptionMessage msg = new ExceptionMessage(throwable, message, logLevel);
        msg.printOut(msg.getLogLevel().getPrintStream());
        this.messages.add(msg);
    }

    /**
     * Logs the given throwable as {@link ExceptionMessage} with {@link LogLevel#ERROR}
     *
     * @param message   additional comment on the throwable
     * @param throwable the exception to be reported
     */
    public void exception(@NotNull String message, @NotNull Throwable throwable) {
        ExceptionMessage msg = new ExceptionMessage(throwable, message, LogLevel.ERROR);
        msg.printOut(msg.getLogLevel().getPrintStream());
        this.messages.add(msg);
    }

    public void exceptionFatal(@NotNull String message, @NotNull Throwable throwable) {
        exception(message, throwable, LogLevel.FATAL);
    }

    /**
     * Logs and triggers the specified crash report (Method terminates the application)
     *
     * @param crashReport the specified CrashReport
     */
    public void crash(@NotNull CrashReport crashReport) {
        CrashReportMessage msg = new CrashReportMessage(crashReport);
        msg.printOut(msg.getLogLevel().getPrintStream());
        this.messages.add(msg);
        this.planShutdown();
        try {
            this.save("crash");
        } catch (IOException e) {
            exceptionFatal("Cannot save log file on crash report!", e);
        }
        System.exit(crashReport.hashCode());
    }

    /**
     * Logs and triggers the a {@link CrashReport} created from #message and #throwable
     *
     * @param message   additional comment on the throwable
     * @param throwable the reported throwable
     */
    public void crash(@NotNull String message, @NotNull Throwable throwable) {
        crash(new CrashReport(message, throwable));
    }

    @NotNull
    public LogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * Saves all logged message to the specified file
     *
     * @param logFile the file that all captured log messages ({@link #messages}) should be written to
     * @throws IOException                if file access fails
     * @throws FileAlreadyExistsException if the file that the log should be written to does already exist
     */
    public void save(@NotNull File logFile) throws IOException, FileAlreadyExistsException {
        if (logFile.exists())
            throw new FileAlreadyExistsException(logFile.getPath());
        else {
            logFile.getParentFile().mkdirs();
            if (logFile.getParentFile().exists())
                logFile.createNewFile();
            else throw new IOException("Could not create directory " + logFile.getParentFile().getPath() + "!");
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
        ArrayList<Message> messages = this.messages;
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            writer.write(message.toString());
            writer.write("\n");
        }
        writer.flush();
        writer.close();
    }

    /**
     * Saves all logged message to a new log file provided by {@link #logFileHandler}
     *
     * @throws IOException                if file access fails
     * @throws FileAlreadyExistsException if the file that the log should be written to does already exist
     */
    public void save(@NotNull String applicationState) throws IOException {
        save(logFileHandler.newLogFile(applicationState));
    }

    /**
     * Sets {@link #shutdownPlanned} to true
     */
    public void planShutdown() {
        this.shutdownPlanned = true;
    }

    public boolean isShutdownPlanned() {
        return shutdownPlanned;
    }
}
