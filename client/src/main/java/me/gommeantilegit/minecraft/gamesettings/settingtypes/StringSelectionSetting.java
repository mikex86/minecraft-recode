package me.gommeantilegit.minecraft.gamesettings.settingtypes;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A string setting that stores a string value of a given pre defined collection of possible string values.
 */
public class StringSelectionSetting extends StringSetting {

    /**
     * Stores the possible values of {@link #value}
     */
    @NotNull
    private final String[] possibleValues;

    /**
     * @param name           sets {@link #name}
     * @param possibleValues sets {@link #possibleValues}
     */
    public StringSelectionSetting(@NotNull String name, @NotNull String[] possibleValues) {
        super(name);
        this.possibleValues = possibleValues;
    }

    @Override
    public StringSelectionSetting setValue(String value) {
        for (String possibleValue : possibleValues) {
            if (possibleValue.equals(value)) {
                return (StringSelectionSetting) super.setValue(value);
            }
        }
        throw new IllegalArgumentException("String value \"" + value + "\" is not contained in value list: " + Arrays.toString(possibleValues) + ".");
    }

    @NotNull
    public String[] getPossibleValues() {
        return possibleValues;
    }

    /**
     * Sets the value of the setting to the next possible value of the {@link #possibleValues} array
     */
    public void setNextValue() {
        setValue(possibleValues[(Arrays.asList(this.possibleValues).indexOf(value) + 1) % possibleValues.length]);
    }

    @Override
    public StringSelectionSetting addValueChangedListener(@NotNull SettingValueChangedListener<String> changedListener) {
        return (StringSelectionSetting) super.addValueChangedListener(changedListener);
    }
}
