package me.gommeantilegit.minecraft.utils;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents a property which has it's value computed on first access
 *
 * @param <T> the type of value the property holds
 */
public class LazyProperty<T> {

    /**
     * Supplies the lazy property with the instance to hold onto
     */
    @NotNull
    private final Supplier<T> supplier;

    /**
     * The value the property holds (if initialized)
     */
    private T value;

    /**
     * State of the lazy property being initialized
     */
    private boolean initialized;

    public LazyProperty(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * @return the value. First access invokes {@link #supplier}. Be aware of performance implications
     */
    public T get() {
        if (!this.initialized) {
            this.value = this.supplier.get();
            this.initialized = true;
        }
        return this.value;
    }
}
