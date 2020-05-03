package me.gommeantilegit.minecraft.server.console.command.impl;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.server.console.command.Command;
import org.jetbrains.annotations.NotNull;

import static me.gommeantilegit.minecraft.utils.MathHelper.humanReadableByteCount;

public class StatsCommand extends Command {

    /**
     * Parent Server Minecraft instance
     */
    @NotNull
    private final ServerMinecraft mc;

    public StatsCommand(@NotNull ServerMinecraft mc) {
        super("stats", "Prints performance stats of the server's runtime", "stats");
        this.mc = mc;
    }

    @Override
    public String onCommand(@NotNull String[] args) {
        return "ServerStats:\n\tUsed Memory: " + humanReadableByteCount(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(), true) + "\n\tFree Memory: " + humanReadableByteCount(Runtime.getRuntime().freeMemory(), true) + "\n\tTotal Memory: " + humanReadableByteCount(Runtime.getRuntime().totalMemory(), true) + "\n\tMax-memory: " + humanReadableByteCount(Runtime.getRuntime().totalMemory(), true) + "\nWorld-Stats:\n\tTick: " + mc.getTimer().getCurrentTicksPerSecond() + " tps";
    }
}
