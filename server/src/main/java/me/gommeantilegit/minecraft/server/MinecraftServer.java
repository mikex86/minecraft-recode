package me.gommeantilegit.minecraft.server;

import me.gommeantilegit.minecraft.ServerMinecraft;

public class MinecraftServer {

    public static void main(String[] args) {
        ServerMinecraft serverMinecraft = new ServerMinecraft();
        serverMinecraft.loadGame();
    }

}
