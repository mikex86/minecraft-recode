package me.gommeantilegit.minecraft.utils.cache.request.result;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a the result to a request performed on a cache to retrieve an element.
 * @param <T> the type of element requested
 */
public class CacheValueRequestResult<T> {

    /**
     * Value of the element requested.
     * May be null, because the element's value is actually null,
     * or it may be null because it was not found.
     */
    @Nullable
    private final T value;

    /**
     * State if the element was found in the cache. If this is false, {@link #isValuePresent()} will return false.
     * Access of {@link #value} will always return null.
     */
    private final boolean valueFound;

    /**
     * @param value sets {@link #value}
     * @param valueFound sets {@link #valueFound}
     */
    public CacheValueRequestResult(@Nullable T value, boolean valueFound) {
        this.value = value;
        this.valueFound = valueFound;
    }

    /**
     * @return true, if the value {@link #value} is valid to access. The request was successful and the value was found.
     */
    public boolean isValuePresent(){
        return valueFound;
    }

    public boolean isValueFound() {
        return valueFound;
    }

    @Nullable
    public T getValue() {
        return value;
    }
}
