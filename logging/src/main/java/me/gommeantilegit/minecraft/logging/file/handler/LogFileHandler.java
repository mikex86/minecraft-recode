package me.gommeantilegit.minecraft.logging.file.handler;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handler object for assigning log files where logs are saved
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class LogFileHandler {

    @NotNull
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd_MM_HH_mm_SS");

    /**
     * Directory where all log files should be stored
     */
    @NotNull
    private final File logFileDirectory;

    /**
     * @param logFileDirectory the directory where future log files should be put
     * @throws AssertionError if the specified file does exist and is not a directory
     * @throws IOException    if the specified directory cannot be created if it does not exist
     */
    public LogFileHandler(@NotNull File logFileDirectory) throws IOException {
        assert (logFileDirectory.exists() && logFileDirectory.isDirectory()) || !logFileDirectory.exists();
        this.logFileDirectory = logFileDirectory;
        if (!logFileDirectory.exists())
            logFileDirectory.mkdirs();
        if (!logFileDirectory.exists())
            throw new IOException("Cannot create directory " + logFileDirectory.getPath() + "!");
    }

    /**
     * @param applicationExitState the state how the application terminated (eg. "crash", "stop", "close")
     * @return a new log file in {@link #logFileDirectory}
     */
    @NotNull
    public File newLogFile(@NotNull String applicationExitState) {
        this.logFileDirectory.mkdirs();
        return new File(logFileDirectory, SDF.format(new Date()) + "_" + applicationExitState + ".log");
    }

    @NotNull
    public File getLogFileDirectory() {
        return logFileDirectory;
    }
}
