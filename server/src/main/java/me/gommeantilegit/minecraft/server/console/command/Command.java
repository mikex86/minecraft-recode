package me.gommeantilegit.minecraft.server.console.command;

import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a command that a user can use in the console
 */
@SideOnly(side = Side.SERVER)
public abstract class Command {

    /**
     * The name of the command
     */
    @NotNull
    private final String name;

    /**
     * Human language description of the command
     */
    @NotNull
    private final String description;

    /**
     * Syntax of the command
     */
    @NotNull
    private final String syntax;

    public Command(@NotNull String name, @NotNull String description, @NotNull String syntax) {
        this.name = name;
        this.description = description;
        this.syntax = syntax;
    }

    /**
     * Invoked when a user uses this command. This method should also perform the commands actions
     *
     * @param args the arguments that the command was invoked with. args[0] is always the name of the command
     * @return the message from the command to be print as a response.
     * If the message starts with "ERROR: " everything that comes afterwards will be print to the console.
     * Every other message will be print to the standard output stream of the console unchanged.
     */
    public abstract String onCommand(@NotNull String[] args);

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public String getSyntax() {
        return syntax;
    }
}
