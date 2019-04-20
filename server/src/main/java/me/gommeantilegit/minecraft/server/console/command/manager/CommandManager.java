package me.gommeantilegit.minecraft.server.console.command.manager;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.server.console.command.Command;
import me.gommeantilegit.minecraft.server.console.command.impl.GarbageCollectionCommand;
import me.gommeantilegit.minecraft.server.console.command.impl.StatsCommand;
import me.gommeantilegit.minecraft.server.console.command.impl.StopCommand;
import me.gommeantilegit.minecraft.server.console.command.impl.TeleportCommand;
import org.jetbrains.annotations.NotNull;

public class CommandManager {

    /**
     * Stores all commands
     */
    @NotNull
    private final Command[] commands;

    public CommandManager(@NotNull ServerMinecraft mc) {
        this.commands = new Command[]{
                new StopCommand(mc),
                new TeleportCommand(mc),
                new StatsCommand(mc),
                new GarbageCollectionCommand()
        };
    }


    @NotNull
    public Command[] getCommands() {
        return commands;
    }
}
