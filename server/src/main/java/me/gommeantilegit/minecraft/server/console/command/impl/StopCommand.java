package me.gommeantilegit.minecraft.server.console.command.impl;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.server.console.command.Command;
import org.jetbrains.annotations.NotNull;

public class StopCommand extends Command {

    @NotNull
    private final ServerMinecraft serverMinecraft;

    public StopCommand(@NotNull ServerMinecraft serverMinecraft) {
        super("stop", "This command stops the server.", "stop");
        this.serverMinecraft = serverMinecraft;
    }

    @Override
    public String onCommand(@NotNull String[] args) {
        this.serverMinecraft.stop();
        return "Shutting down server...";
    }
}
