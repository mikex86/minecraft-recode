package me.gommeantilegit.minecraft.ui.button;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.LimitedNumberSetting;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.PercentSetting;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.min;
import static java.lang.StrictMath.max;

/**
 * Represents a slider that controls the setting value of a {@link PercentSetting}
 */
public class Slider extends GuiButton {

    /**
     * Parent setting
     */
    @NotNull
    private final PercentSetting setting;

    public Slider(@NotNull PercentSetting setting, int width, int height, int posX, int posY, ClientMinecraft mc) {
        super("", width, height, posX, posY, mc);
        this.setting = setting;
        updateDisplayText();
    }

    public Slider(@NotNull PercentSetting setting, int posX, int posY, ClientMinecraft mc) {
        super("", 150, 20, posX, posY, mc);
        this.setting = setting;
        updateDisplayText();
    }

    @Override
    public void render(float mouseX, float mouseY) {
        super.render(mouseX, mouseY);
        if (pressed) {
            LimitedNumberSetting.Interval interval = setting.getInterval();
            double min = interval.getMin(), max = interval.getMax();
            double dif = max - min;
            float sliderValue = (mouseX - (posX + 4)) / (float) (width - 8);
            double val = min(max(0, dif * sliderValue), max - min);
            setting.setValue(min + val);
            updateDisplayText();
        }
    }

    @Override
    public void draw(boolean hovered) {
//        drawTexturedModalRect(xPosition + (int)(sliderValue * (float)(width - 8)), yPosition, 0, 66, 4, 20);
//        drawTexturedModalRect(xPosition + (int)(sliderValue * (float)(width - 8)) + 4, yPosition, 196, 66, 4, 20);
        this.hovered = hovered;
        this.textureWrapper.imageView.spriteBatch.begin();
        this.textureWrapper.drawUVRect(posX, posY, width / 2f, height, 0, 46, width / 2, height, 1, 1, 1, 1, false);
        this.textureWrapper.drawUVRect(posX + width / 2f, posY, width / 2f, height, 200 - width / 2, 46, width / 2, height, 1, 1, 1, 1, false);
        this.textureWrapper.drawUVRect(posX + (int) (setting.getRelativeValue() * (float) (width - 8)), posY, 4, 20, 0, 66, 4, 20, 1, 1, 1, 1, false);
        this.textureWrapper.drawUVRect(posX + (int) (setting.getRelativeValue() * (float) (width - 8)) + 4, posY, 4, 20, 196, 66, 4, 20, 1, 1, 1, 1, false);
        this.textureWrapper.imageView.spriteBatch.end();
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
    protected String getDisplayText(PercentSetting selectionSetting) {
        String val = this.setting.getValueStringReplacementMap().getOrDefault(selectionSetting.getValue(), setting.getValue().intValue() + "%");
        return selectionSetting.getName() + ": " + val;
    }
}
