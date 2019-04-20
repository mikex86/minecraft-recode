package me.gommeantilegit.minecraft.ui.screen.impl;

import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.ui.button.GuiButton;
import me.gommeantilegit.minecraft.ui.screen.GuiScreen;
import org.jetbrains.annotations.NotNull;

public class GuiOptions extends GuiScreen {

    /**
     * The title string ("Options")
     */
    @NotNull
    private final String titleString = "Options";

    /**
     * Font String width of {@link #titleString}
     */
    private int titleStringWidth;

    /**
     * The GuiScreen instance that was the previous guiScreen
     */
    @NotNull
    private final GuiScreen prevScreen;

    public GuiOptions(@NotNull GuiScreen prevScreen) {
        this.prevScreen = prevScreen;
    }

    @Override
    public void initGui(int scaledWidth, int scaledHeight) {
        super.initGui(scaledWidth, scaledHeight);
        this.addOptions(scaledWidth, scaledHeight, mc.gameSettings.generalSettings.settingsList);
        this.buttons.add(new GuiButton("Video Settings...", scaledWidth / 2 - 100, scaledHeight / 6 + 96 + 12, mc).setOnMouseDownListener(p -> mc.uiManager.displayGuiScreen(new GuiVideoSettings(this))));
        this.buttons.add(new GuiButton("Controls...", scaledWidth / 2 - 100, scaledHeight / 6 + 120 + 12, mc));
        this.buttons.add(new GuiButton("Done", scaledWidth / 2 - 100, scaledHeight / 6 + 168, mc).setOnMouseDownListener(pointer -> mc.uiManager.displayGuiScreen(prevScreen)));
        this.titleStringWidth = mc.uiManager.fontRenderer.getStringWidth(titleString);
    }

    @Override
    public void render() {
        drawDefaultBackground();
        mc.uiManager.fontRenderer.drawStringWithShadow(titleString, DPI.scaledWidthi / 2f - titleStringWidth / 2f, 20, 0xffffff);
        super.render();
    }

}
