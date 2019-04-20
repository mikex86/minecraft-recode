package me.gommeantilegit.minecraft.logging.message;

import me.gommeantilegit.minecraft.logging.LogLevel;
import me.gommeantilegit.minecraft.logging.crash.CrashReport;
import org.jetbrains.annotations.NotNull;

/**
 * Log Message for a crash report
 */
public class CrashReportMessage extends Message {

    public CrashReportMessage(@NotNull CrashReport crashReport) {
        super("[CRASH-REPORT]", crashReport.toString(), LogLevel.FATAL, Thread.currentThread());
    }
}
