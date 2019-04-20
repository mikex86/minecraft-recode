package me.gommeantilegit.minecraft.gamesettings.settingtypes;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an object with a changeable property of type T
 *
 * @param <T> the type of the changable value
 */
public class Setting<T> {

    /**
     * List of all listeners invoked on value change
     */
    @NotNull
    private final List<SettingValueChangedListener<T>> valueChangedListeners = new ArrayList<>();

    /**
     * The name of the setting
     */
    @NotNull
    private final String name;

    /**
     * The changeable value of the setting
     */
    protected T value;

    public Setting(@NotNull String name) {
        this.name = name;
    }

    public Setting<T> setValue(T value) {
        valueChangedListeners.forEach(l -> l.onValueChanged(value));
        this.value = value;
        return this;
    }

    /**
     * Registers a new listener that is invoked when the value of the setting changes
     *
     * @param changedListener the listener to be registered
     * @return this instance for the purpose of chaining method calls
     */
    public Setting<T> addValueChangedListener(@NotNull SettingValueChangedListener<T> changedListener) {
        this.valueChangedListeners.add(changedListener);
        return this;
    }

    public T getValue() {
        return value;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public interface SettingValueChangedListener<T> {

        /**
         * Invoked on setting value change
         *
         * @param value the new value of the setting
         */
        void onValueChanged(T value);

    }
}
