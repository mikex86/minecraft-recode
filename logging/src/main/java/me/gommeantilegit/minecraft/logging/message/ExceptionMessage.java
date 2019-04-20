package me.gommeantilegit.minecraft.logging.message;

import me.gommeantilegit.minecraft.logging.LogLevel;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ExceptionMessage extends Message {

    public ExceptionMessage(@NotNull Throwable exception, @NotNull String message, @NotNull LogLevel logLevel) {
        super("[EXCEPTION]", "Exception thrown: " + exception.getClass().getName() + "! " + message + ".\n " + toString(exception), logLevel, Thread.currentThread());
    }

    public ExceptionMessage(@NotNull Throwable throwable, @NotNull String message) {
        this(throwable, message, LogLevel.ERROR);
    }

    @NotNull
    private static String toString(@NotNull Throwable exception) {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytesOut);
        exception.printStackTrace(stream);
        return new String(bytesOut.toByteArray());
    }
}
