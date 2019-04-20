package me.gommeantilegit.minecraft.server.console.command.impl;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerMP;
import me.gommeantilegit.minecraft.server.console.command.Command;
import org.jetbrains.annotations.NotNull;

public class TeleportCommand extends Command {

    /**
     * Server minecraft instance
     */
    @NotNull
    private final ServerMinecraft mc;

    public TeleportCommand(@NotNull ServerMinecraft mc) {
        super("tp", "Teleports a player to a given position", "tp <PlayerName> x y z");
        this.mc = mc;
    }

    @Override
    public String onCommand(@NotNull String[] args) {
        if (args.length != 5) {
            return "ERROR: Invalid syntax. Please use: " + getSyntax();
        }
        String playerName = args[1];
        float x, y, z;
        try {
            x = Float.valueOf(args[2]);
            y = Float.valueOf(args[3]);
            z = Float.valueOf(args[4]);
        } catch (NumberFormatException e) {
            return "ERROR: Invalid number entered. " + e.getLocalizedMessage();
        }
        EntityPlayerMP player = mc.nettyServer.netHandlerPlayServer.getPlayerByName(playerName);
        if (player == null) {
            return "ERROR: No player called " + playerName + " found.";
        }
        player.setPosition(x, y, z);
        return "Player teleported!";
    }
}
