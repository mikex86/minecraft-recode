package me.gommeantilegit.minecraft.gamesettings.settingtypes;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a setting with changeable string value.
 */
public class StringSetting extends Setting<String> {
    /**
     * @param name sets {@link #name}
     */
    public StringSetting(@NotNull String name) {
        super(name);
    }

    @Override
    public StringSetting setValue(String value) {
        return (StringSetting) super.setValue(value);
    }

    @Override
    public StringSetting addValueChangedListener(@NotNull SettingValueChangedListener<String> changedListener) {
        return (StringSetting) super.addValueChangedListener(changedListener);
    }
}
