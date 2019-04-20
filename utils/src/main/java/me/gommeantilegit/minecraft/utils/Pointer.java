package me.gommeantilegit.minecraft.utils;

/**
 * Pointer pointing at value type of T
 *
 * @param <T> the type of the value pointed at
 */
public class Pointer<T> {

    /**
     * Value that the pointer points at
     */
    public T value;

    public Pointer() {
    }

    public Pointer(T t) {
        this.value = t;
    }

}
