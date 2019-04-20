package me.gommeantilegit.minecraft.ui.screen.impl;

import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.ui.button.GuiButton;
import me.gommeantilegit.minecraft.ui.screen.GuiScreen;
import org.jetbrains.annotations.NotNull;

public class GuiVideoSettings extends GuiScreen {

    /**
     * The title string ("Video Settings")
     */
    @NotNull
    private final String titleString = "Video Settings";

    /**
     * Font String width of {@link #titleString}
     */
    private int titleStringWidth;

    /**
     * The {@link GuiOptions} instance that was the previous guiScreen
     */
    @NotNull
    private final GuiOptions guiOptions;

    public GuiVideoSettings(@NotNull GuiOptions guiOptions) {
        this.guiOptions = guiOptions;
    }

    @Override
    public void initGui(int scaledWidth, int scaledHeight) {
        super.initGui(scaledWidth, scaledHeight);
        this.addOptions(scaledWidth, scaledHeight, mc.gameSettings.videoSettings.settingsList);
        this.buttons.add(new GuiButton("Done", scaledWidth / 2 - 100, scaledHeight / 6 + 168, mc).setOnMouseDownListener(pointer -> mc.uiManager.displayGuiScreen(guiOptions)));
        this.titleStringWidth = mc.uiManager.fontRenderer.getStringWidth(titleString);
    }

    @Override
    public void render() {
        drawDefaultBackground();
        mc.uiManager.fontRenderer.drawStringWithShadow(titleString, DPI.scaledWidthi / 2f - titleStringWidth / 2f, 20, 0xffffff);
        super.render();
    }

}
