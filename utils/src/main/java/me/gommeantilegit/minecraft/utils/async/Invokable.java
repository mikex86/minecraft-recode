package me.gommeantilegit.minecraft.utils.async;

import org.jetbrains.annotations.NotNull;

/**
 * Represents something that is invoked with a given value of type T
 * @param <T> the type of value the {@link #invoke(Object)} method is invoked with
 */
public interface Invokable<T> {

    /**
     * Called to invoke on value
     * @param t the value
     */
    void invoke(@NotNull T t);

}
