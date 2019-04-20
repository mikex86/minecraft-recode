package me.gommeantilegit.minecraft.ui.button;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.StringSelectionSetting;
import org.jetbrains.annotations.NotNull;

/**
 * A button wrapped around a {@link StringSelectionSetting}.
 * The button changes the value of the setting on click to the next possible setting value.
 */
public class StringValueSwitchButton extends GuiButton {

    /**
     * The parent {@link StringSelectionSetting} instance
     */
    @NotNull
    private final StringSelectionSetting setting;

    public StringValueSwitchButton(@NotNull StringSelectionSetting selectionSetting, int width, int height, int posX, int posY, ClientMinecraft mc) {
        super("", width, height, posX, posY, mc);
        this.setting = selectionSetting;
        updateDisplayText();
    }

    public StringValueSwitchButton(@NotNull StringSelectionSetting selectionSetting, int posX, int posY, ClientMinecraft mc) {
        super("", 150, 20, posX, posY, mc);
        this.setting = selectionSetting;
        updateDisplayText();
    }

    @Override
    protected void onMouseDown() {
        super.onMouseDown();
        this.setting.setNextValue();
        updateDisplayText();
    }

    private void updateDisplayText() {
        setDisplayText(getDisplayText(setting));
    }

    /**
     * Called to determine the text to display on the button
     *
     * @param selectionSetting the parent setting instance
     * @return the display string to represent the value of the setting for the user
     */
    @NotNull
    protected String getDisplayText(StringSelectionSetting selectionSetting) {
        return selectionSetting.getName() + ": " + selectionSetting.getValue();
    }
}
