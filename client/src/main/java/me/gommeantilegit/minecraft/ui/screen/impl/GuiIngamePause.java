package me.gommeantilegit.minecraft.ui.screen.impl;

import com.badlogic.gdx.Gdx;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.ui.button.GuiButton;
import me.gommeantilegit.minecraft.ui.screen.GuiScreen;
import org.jetbrains.annotations.Nullable;

public class GuiIngamePause extends GuiScreen {

    @Override
    public void initGui(int scaledWidth, int scaledHeight) {
        super.initGui(scaledWidth, scaledHeight);
        int i = -16;
        this.buttons.add(new GuiButton("Save and Quit to Title", scaledWidth / 2 - 100, scaledHeight / 4 + 120 + i, mc).setOnMouseDownListener(p -> {
            mc.theWorld.stopAsyncWork();
            mc.theWorld = null;
            mc.thePlayer.setWorld(null);
            mc.nettyClient.interrupt();
            mc.nettyClient = null;
            mc.uiManager.displayGuiScreen(new GuiMainMenu());
        }));

        this.buttons.add(new GuiButton("Back to Game", scaledWidth / 2 - 100, scaledHeight / 4 + 24 + i, mc).setOnMouseDownListener(p -> mc.uiManager.displayGuiScreen(null)));
        this.buttons.add(new GuiButton("Options...", 98, 20, scaledWidth / 2 - 100, scaledHeight / 4 + 96 + i, mc).setOnMouseDownListener(p -> mc.uiManager.displayGuiScreen(new GuiOptions(this))));
        GuiButton shareLan;
        this.buttons.add(shareLan = new GuiButton("Open to LAN", 98, 20, scaledWidth / 2 + 2, scaledHeight / 4 + 96 + i, mc));
        this.buttons.add(new GuiButton("Achievements", 98, 20, scaledWidth / 2 - 100, scaledHeight / 4 + 48 + i, mc));
        this.buttons.add(new GuiButton("Statistics", 98, 20, scaledWidth / 2 + 2, scaledHeight / 4 + 48 + i, mc));
        shareLan.setDisabled(true);
        mc.inputHandler.unregisterProcessor(mc.thePlayer.camController);
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void render() {
        this.drawGradientRect(0, 0, DPI.scaledWidth, DPI.scaledHeight, 0xc0101010, 0xd0101010);
        super.render();
    }

    @Override
    public void onGuiClosed(@Nullable GuiScreen newScreen) {
        if (newScreen == null) {
            Gdx.input.setCursorCatched(true);
            mc.inputHandler.registerInputProcessor(mc.thePlayer.camController);
        }
    }
}
