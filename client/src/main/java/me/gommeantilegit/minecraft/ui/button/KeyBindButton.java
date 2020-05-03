package me.gommeantilegit.minecraft.ui.button;

import com.badlogic.gdx.Input;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.KeyBindSetting;
import org.jetbrains.annotations.NotNull;

/**
 * A button wrapped around a {@link KeyBindSetting}.
 * The button changes the value of the setting on click to the next pressed key.
 */
public class KeyBindButton extends GuiButton {

    /**
     * The parent {@link KeyBindSetting} instance
     */
    @NotNull
    private final KeyBindSetting setting;

    /**
     * State of the button listening to key presses
     */
    private boolean listening;

    public KeyBindButton(@NotNull KeyBindSetting keyBindSetting, int width, int height, int posX, int posY, ClientMinecraft mc) {
        super("", width, height, posX, posY, mc);
        this.setting = keyBindSetting;
        updateDisplayText();
    }

    public KeyBindButton(@NotNull KeyBindSetting keyBindSetting, int posX, int posY, ClientMinecraft mc) {
        super("", 150, 20, posX, posY, mc);
        this.setting = keyBindSetting;
        updateDisplayText();
    }

    @Override
    protected void onMouseDown() {
        super.onMouseDown();
        this.listening = true;
        updateDisplayText();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        this.listening = false;
        updateDisplayText();
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean keyDown(int keyCode) {
        if (this.listening) {
            this.setting.setValue(keyCode);
            this.listening = false;
            updateDisplayText();
        }
        return super.keyDown(keyCode);
    }

    private void updateDisplayText() {
        setDisplayText(getDisplayText(setting));
    }

    /**
     * Called to determine the text to display on the button
     *
     * @param keyBindSetting the parent setting instance
     * @return the display string to represent the value of the setting for the user
     */
    @NotNull
    protected String getDisplayText(@NotNull KeyBindSetting keyBindSetting) {
        return (this.listening ? "> " : "") + Input.Keys.toString(keyBindSetting.getValue()) + (this.listening ? " <" : "");
    }
}
