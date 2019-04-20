package me.gommeantilegit.minecraft.gamesettings.settingtypes;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Represents a setting that can store an integer number that is a percent value
 */
public class PercentSetting extends LimitedNumberSetting {

    /**
     * Value replacement map for percent values.
     * Setting displayer needs to replace the value with the parent string
     */
    @NotNull
    private final HashMap<Double, String> valueStringReplacementMap = new HashMap<>();

    /**
     * Constructor constructs a setting instance that can store values from 0 to 100
     *
     * @param name sets {@link #name}
     */
    public PercentSetting(@NotNull String name) {
        super(name, true, new Interval(0, 100));
    }

    /**
     * Constructor constructs a setting instance that can store contained in the specified interval
     *
     * @param name     sets {@link #name}
     * @param interval the given value interval of allowed percent values
     */
    public PercentSetting(@NotNull String name, @NotNull Interval interval) {
        super(name, true, interval);
    }

    /**
     * Adds a replacement entry to replace the specified key value with the specified string value on setting value display to the user
     * @param key the value to replace
     * @param value the string to replace it with
     * @return this instance for method chaining
     */
    @NotNull
    public PercentSetting addStringValueReplacement(double key, String value) {
        this.valueStringReplacementMap.put(key, value);
        return this;
    }

    /**
     * @return a relative representation of the settings value in the interval [0; 1] scaling to [min; max]
     */
    public double getRelativeValue(){
        return this.getInterval().getRelativeValue(value);
    }

    @Override
    public PercentSetting addValueChangedListener(@NotNull SettingValueChangedListener<Double> changedListener) {
        return (PercentSetting) super.addValueChangedListener(changedListener);
    }

    @Override
    public PercentSetting setValue(Double value) {
        return (PercentSetting) super.setValue(value);
    }

    @NotNull
    public HashMap<Double, String> getValueStringReplacementMap() {
        return valueStringReplacementMap;
    }
}
