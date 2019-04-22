package me.gommeantilegit.minecraft.gamesettings.settingtypes;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a setting that can store boolean SOUND_RESOURCES. Eg. whether something is enabled or not.
 */
public class BooleanSetting extends Setting<Boolean> {
    /**
     * @param name sets {@link #name}
     */
    public BooleanSetting(@NotNull String name) {
        super(name);
    }

    @Override
    public BooleanSetting setValue(Boolean value) {
        return (BooleanSetting) super.setValue(value);
    }

    @Override
    public BooleanSetting addValueChangedListener(@NotNull SettingValueChangedListener<Boolean> changedListener) {
        return (BooleanSetting) super.addValueChangedListener(changedListener);
    }
}
