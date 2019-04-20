package me.gommeantilegit.minecraft.ui.screen.impl;

import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.ui.button.GuiButton;
import me.gommeantilegit.minecraft.ui.screen.GuiScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiConnectionFailed extends GuiScreen {

    /**
     * Title text error
     */
    @NotNull
    private final String title;

    /**
     * Second more specific error message
     */
    @NotNull
    private final String errorMessage;

    /**
     * String widths for {@link #title} and {@link #errorMessage}
     */
    private int titleStringWidth, errorMessageStringWidth;

    public GuiConnectionFailed(@NotNull String title, @Nullable String errorMessage) {
        this.title = title;
        this.errorMessage = "" + errorMessage;
    }

    @Override
    public void initGui(int scaledWidth, int scaledHeight) {
        super.initGui(scaledWidth, scaledHeight);
        this.buttons.add(new GuiButton("Back to title screen", scaledWidth / 2 - 100, scaledHeight / 4 + 120 + 12, mc).setOnMouseDownListener(p -> mc.uiManager.displayGuiScreen(new GuiMainMenu())));
        this.titleStringWidth = mc.uiManager.fontRenderer.getStringWidth(title);
        this.errorMessageStringWidth = mc.uiManager.fontRenderer.getStringWidth(errorMessage);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void render() {
        drawDefaultBackground();
        mc.uiManager.fontRenderer.drawStringWithShadow(title, DPI.scaledWidthi / 2 - titleStringWidth / 2, DPI.scaledHeighti / 2 - 50, 0xffffff);
        mc.uiManager.fontRenderer.drawStringWithShadow(errorMessage, DPI.scaledWidthi / 2 - errorMessageStringWidth / 2, DPI.scaledHeighti / 2 - 10, 0xffffff);
        super.render();
    }
}
