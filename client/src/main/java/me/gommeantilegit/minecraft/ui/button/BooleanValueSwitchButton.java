package me.gommeantilegit.minecraft.ui.button;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.BooleanSetting;
import org.jetbrains.annotations.NotNull;

/**
 * A GuiButton wrapped around a {@link BooleanSetting}.
 */
public class BooleanValueSwitchButton extends GuiButton {

    /**
     * Parent {@link BooleanSetting} instance
     */
    @NotNull
    private final BooleanSetting booleanSetting;

    public BooleanValueSwitchButton(@NotNull BooleanSetting booleanSetting, int width, int height, int posX, int posY, ClientMinecraft mc) {
        super("", width, height, posX, posY, mc);
        this.booleanSetting = booleanSetting;
        updateDisplayText();
    }

    public BooleanValueSwitchButton(@NotNull BooleanSetting booleanSetting, int posX, int posY, ClientMinecraft mc) {
        super("", 150, 20, posX, posY, mc);
        this.booleanSetting = booleanSetting;
        updateDisplayText();
    }

    private void updateDisplayText() {
        setDisplayText(getDisplayText(booleanSetting));
    }

    @Override
    protected void onMouseDown() {
        super.onMouseDown();
        booleanSetting.setValue(!booleanSetting.getValue());
        updateDisplayText();
    }


    /**
     * Called to determine the text to display on the button
     *
     * @param selectionSetting the parent setting instance
     * @return the display string to represent the value of the setting for the user
     */
    @NotNull
    protected String getDisplayText(BooleanSetting selectionSetting) {
        return selectionSetting.getName() + ": " + selectionSetting.getValue();
    }
}
