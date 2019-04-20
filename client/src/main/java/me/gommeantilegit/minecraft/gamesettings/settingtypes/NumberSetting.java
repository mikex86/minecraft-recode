package me.gommeantilegit.minecraft.gamesettings.settingtypes;

import org.jetbrains.annotations.NotNull;

/**
 * A numeric setting
 */
public class NumberSetting extends Setting<Double> {

    /**
     * State whether the setting only accepts integer values
     */
    private final boolean integerValuesOnly;

    /**
     * @param name sets {@link #name}
     * @param integerValuesOnly sets {@link #integerValuesOnly}
     */
    public NumberSetting(@NotNull String name, boolean integerValuesOnly) {
        super(name);
        this.integerValuesOnly = integerValuesOnly;
    }

    @Override
    public NumberSetting addValueChangedListener(@NotNull SettingValueChangedListener<Double> changedListener) {
        return (NumberSetting) super.addValueChangedListener(changedListener);
    }

    @Override
    public NumberSetting setValue(Double value) {
        return (NumberSetting) super.setValue((double) value.intValue());
    }

    @Override
    public Double getValue() {
        return super.getValue();
    }
}
