package me.gommeantilegit.minecraft.gamesettings.settingtypes;

import org.jetbrains.annotations.NotNull;

/**
 * A setting that stores a keybind keycode
 */
public class KeyBindSetting extends Setting<Integer> {

    public KeyBindSetting(@NotNull String name, int defaultValue) {
        super(name);
        this.setValue(defaultValue);
    }
}
