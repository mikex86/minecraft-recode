package me.gommeantilegit.minecraft.server.console.command.impl;

import me.gommeantilegit.minecraft.server.console.command.Command;
import org.jetbrains.annotations.NotNull;

public class GarbageCollectionCommand extends Command {

    public GarbageCollectionCommand() {
        super("gc", "Gives a recommendation to the JVM to perform garbage collection. This might or might not actually be done.", "gc");
    }

    @Override
    public String onCommand(@NotNull String[] args) {
        System.gc();
        return "Garbage collection was invoked.";
    }
}
