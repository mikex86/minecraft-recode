package me.gommeantilegit.minecraft.logging.crash;

import me.gommeantilegit.minecraft.utils.Hardware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

/**
 * Represents a crash triggered on code failure
 */
public class CrashReport {

    /**
     * System info instance
     */
    @NotNull
    private static final SystemInfo systemInfo = new SystemInfo();

    static {
        systemInfo.getHardware();
        systemInfo.getOperatingSystem();
    }

    /**
     * Additional message on the throwable
     */
    @NotNull
    private final String message;

    /**
     * Reported Throwable
     */
    @NotNull
    private final Throwable throwable;

    /**
     * The Thread that the exception was thrown on
     */
    @NotNull
    private final Thread thread;

    /**
     * The StackTrace up to the creation of the crash report
     */
    @NotNull
    private final StackTraceElement[] crashReportCreationStackTrace;

    /**
     * @param message   sets {@link #message}
     * @param throwable sets {@link #throwable}
     */
    public CrashReport(@NotNull String message, @NotNull Throwable throwable) {
        this.message = message;
        this.throwable = throwable;
        this.thread = Thread.currentThread();
        this.crashReportCreationStackTrace = Thread.currentThread().getStackTrace();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Oh, no! Looks like Minecraft has crashed!\n\nMessage: \"" + message + "\"\n");
        str.append("\nSystem info: \n");
        printSystemInfo(str);
        str.append("\nThread: ").append(thread.toString()).append("\n");
        str.append("StackTrace to Crash-Report trigger point: \n");
        for (StackTraceElement element : this.crashReportCreationStackTrace) {
            str.append("\tat ").append(element).append("\n");
        }
        str.append("\n");
        str.append("\nExceptions:\n");
        for (Throwable exception = this.throwable; exception != null; exception = exception.getCause()) {
            if (exception == this.throwable) {
                str.append("\n\tMain Throable: \n");
            } else {
                str.append("\n\tCaused by:");
            }
            str.append("\n\t\tType: ").append(exception.getClass().getSimpleName()).append(" (").append(exception.getClass().getName()).append(")\n");
            if (exception.getCause() != null)
                str.append("\t\tCaused by: ").append(exception.getCause().getClass().getSimpleName()).append(" (").append(exception.getClass().getName()).append(")\n");
            str.append("\t\tMessage: ").append(exception.getLocalizedMessage()).append("\n");
            str.append("\t\tStackTrace: \n\n");
            for (StackTraceElement element : exception.getStackTrace()) {
                str.append("\t\t\tat ").append(element).append("\n");
            }
            str.append("\n");
        }
        return str.toString();
    }

    private void printSystemInfo(@NotNull StringBuilder str) {
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        CentralProcessor cpu = hardware.getProcessor();
        str.append("CPU: ").append(cpu.getFamily()).append(" ").append(cpu.getIdentifier()).append(" ").append(cpu.getProcessorID()).append(" ").append(cpu.getPhysicalProcessorCount()).append("x ").append(cpu.getName()).append("\n");
        str.append("RAM: ").append(humanReadableByteCount(hardware.getMemory().getTotal(), false)).append("\n");
        str.append("GPU-VENDOR: ").append(Hardware.GPU_VENDOR).append("\n");
        str.append("GPU-NAME: ").append(Hardware.GPU_NAME).append("\n");
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        str.append("OS: ").append(operatingSystem.getManufacturer()).append(" ").append(operatingSystem.getFamily()).append(" ").append(operatingSystem.getVersion()).append("\n");
        str.append("OS-Filesystem: ").append(operatingSystem.getFileSystem().getClass().getSimpleName()).append("\n");
    }

    /**
     * Converts the given bytes in a human readable string
     *
     * @param bytes the amount of bytes
     * @param si    the unit to use. si = 1000 else 1024 (binary)
     * @return the human readable string
     */
    @SuppressWarnings("Duplicates")
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    @NotNull
    public Thread getThread() {
        return thread;
    }

    @NotNull
    public Throwable getThrowable() {
        return throwable;
    }

    @Nullable
    public StackTraceElement[] getCrashReportCreationStackTrace() {
        return crashReportCreationStackTrace;
    }

    /**
     * Initializes crash report class
     */
    public static void init() {
    }
}
