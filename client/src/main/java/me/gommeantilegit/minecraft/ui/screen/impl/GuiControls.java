package me.gommeantilegit.minecraft.ui.screen.impl;

import com.badlogic.gdx.Input;
import me.gommeantilegit.minecraft.gamesettings.GameSettings;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.KeyBindSetting;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.ui.screen.GuiScreen;
import org.jetbrains.annotations.NotNull;

public class GuiControls extends GuiScreen {

    /**
     * The game settings instance to wrap and display
     */
    @NotNull
    private final GameSettings gameSettings;

    /**
     * The previous GUI screen
     */
    @NotNull
    private final GuiScreen prevScreen;

    /**
     * The title string ("Options")
     */
    @NotNull
    private final String titleString = "Controls";

    public GuiControls(@NotNull GuiScreen prevScreen, @NotNull GameSettings gameSettings) {
        this.prevScreen = prevScreen;
        this.gameSettings = gameSettings;
    }

    @Override
    public void initGui(int scaledWidth, int scaledHeight) {
        super.initGui(scaledWidth, scaledHeight);
        addOptions(scaledWidth, scaledHeight, this.gameSettings.keyBindings.keyBindList);
    }

    @Override
    public void render() {
        drawDefaultBackground();
        mc.uiManager.fontRenderer.drawCenteredStringWithShadow(titleString, mc.width / 2f, 20, 0xffffff);
        int width = DPI.scaledWidthi, height = DPI.scaledHeighti;
        int x = width / 2 - 155;
        int y = 0;
        for (KeyBindSetting keyBind : this.gameSettings.keyBindings.keyBindList) {
            mc.uiManager.fontRenderer.drawStringWithShadow(keyBind.getName(), x + (y % 2) * 160 + 70 + 6, height / 6 + 24 * (y >> 1) + 7, 0xFFFFFFFF);
            y++;
        }
        super.render();
    }
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            mc.uiManager.displayGuiScreen(prevScreen);
        }
        return super.keyDown(keycode);
    }
}
