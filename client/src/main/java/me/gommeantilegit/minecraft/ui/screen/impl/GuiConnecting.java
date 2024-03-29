package me.gommeantilegit.minecraft.ui.screen.impl;

import com.badlogic.gdx.Gdx;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.netty.NettyClient;
import me.gommeantilegit.minecraft.ui.button.GuiButton;
import me.gommeantilegit.minecraft.ui.screen.GuiScreen;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;

import java.net.ConnectException;
import java.net.UnknownHostException;

import static me.gommeantilegit.minecraft.AbstractMinecraft.STD_PORT;

public class GuiConnecting extends GuiScreen {

    /**
     * The Host address to connect to
     */
    @NotNull
    private final String hostAddress;

    /**
     * The port of the server to connect to
     */
    private final int port;
    private boolean connected = false;

    public GuiConnecting(@NotNull String hostAddress, int port) {
        this.hostAddress = hostAddress;
        this.port = port;
    }

    private void connect() {
        Thread thread = new Thread(() -> {
            mc.nettyClient = new NettyClient(hostAddress, port, mc);
            // Handling exceptions
            mc.nettyClient.setUncaughtExceptionHandler((t, e) -> {
                if (e instanceof UnknownHostException) {
                    mc.uiManager.displayGuiScreen(new GuiConnectionFailed("Failed to connect to the server", "Unknown host '" + hostAddress + "'"));
                } else if (e instanceof ConnectException) {
                    mc.uiManager.displayGuiScreen(new GuiConnectionFailed("Failed to connect to the server", e.getMessage()));
                }
            });
            this.mc.nettyClient.start();
        });
        thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        thread.setDaemon(true);
        thread.start();
        connected = true;
    }

    /**
     * @param args string split args by regex ':'
     */
    public GuiConnecting(String[] args) {
        assert args.length >= 1;
        if (args.length == 1) {
            this.hostAddress = args[0];
            this.port = STD_PORT;
        } else {
            int port = STD_PORT;
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
            }
            this.hostAddress = args[0];
            this.port = port;
        }
    }

    @Override
    public void initGui(int scaledWidth, int scaledHeight) {
        super.initGui(scaledWidth, scaledHeight);
        this.buttons.add(new GuiButton("Cancel", scaledWidth / 2 - 100, scaledHeight / 4 + 120 + 12, mc).setOnMouseDownListener(p -> {
            mc.uiManager.displayGuiScreen(new GuiMainMenu());
            mc.closeServerConnection();
        }));
        if (!connected)
            connect();

    }

    @Override
    public void render() {
        boolean worldLoaded = worldLoaded();
        if (worldLoaded) {
            Gdx.input.setCursorCatched(true);
            mc.inputHandler.registerInputProcessor(mc.thePlayer.camController); // Registering the player cam controller as input processor. Needs to be unregistered on disconnect!
            mc.uiManager.displayGuiScreen(null);
            return;
        }
        drawDefaultBackground(true);
        mc.uiManager.fontRenderer.drawCenteredStringWithShadow(mc.nettyClient == null || mc.nettyClient.channel == null || !mc.nettyClient.channel.isActive() ? "Connecting to the server..." : "Logging in...", DPI.scaledWidth / 2, DPI.scaledHeight / 2 - 50, 0xffffffff);
        super.render();
    }

    private boolean worldLoaded() {
        return mc.theWorld != null && mc.thePlayer.world != null && mc.nettyClient.netHandlerPlayClient.isWorldSetup() && mc.thePlayer.spawned && ((ClientChunk) mc.thePlayer.getCurrentChunk()).getChunkSection((int) mc.thePlayer.posY).hasMesh();
    }
}
