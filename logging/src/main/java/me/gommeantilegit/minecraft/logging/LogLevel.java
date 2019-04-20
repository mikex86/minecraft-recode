package me.gommeantilegit.minecraft.logging;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

/**
 * Enum representing the log levels - priority of messages shown
 */
public enum LogLevel {

    DEBUG(1, System.out), INFO(2, System.out), WARNING(3, System.err), ERROR(4, System.err), FATAL(5, System.err);

    private final int logLevel;

    @NotNull
    private final PrintStream printStream;

    LogLevel(int logLevel, @NotNull PrintStream printStream) {
        this.logLevel = logLevel;
        this.printStream = printStream;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }
}
