package me.gommeantilegit.minecraft.block.state.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

/**
 * Represents a type of optional which value can be null
 *
 * @param <T> the type of value to store
 */
public class NullableOptional<T> {

    /**
     * The instance of optional that is used to represent all un-present values contained in a {@link NullableOptional} instance
     */
    @NotNull
    private static final NullableOptional EMPTY = new NullableOptional();

    /**
     * The optionals value
     */
    @Nullable
    private final T value;

    /**
     * The state whether the optional is empty - meaning it does not store a value. Thus the value cannot be retrieved with function {@link #getValue()} as it will throw a {@link java.util.NoSuchElementException}
     */
    private final boolean empty;

    private NullableOptional(@Nullable T value) {
        this.value = value;
        this.empty = false;
    }

    private NullableOptional() {
        this.value = null;
        this.empty = true;
    }

    public T getValue() {
        if (empty)
            throw new NoSuchElementException("Value for NullableOptional not present!");
        return value;
    }

    /**
     * @param value the value that an optional should be wrapped around
     * @param <T>   the type of value to wrap an optional around
     * @return a present optional instance wrapped around the specified value. Access of value will not result in a {@link NoSuchElementException}
     */
    @NotNull
    public static <T> NullableOptional<T> of(@Nullable T value) {
        return new NullableOptional<>(value);
    }

    /**
     * @param <T> the type of the value that would be stored in the optional if it were present
     * @return the {@link #EMPTY} instance (An empty optional that will throw a {@link NoSuchElementException} when the value is retrieved)
     */
    @NotNull
    public static <T> NullableOptional<T> empty() {
        return EMPTY;
    }
}
