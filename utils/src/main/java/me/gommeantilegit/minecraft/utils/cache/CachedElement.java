package me.gommeantilegit.minecraft.utils.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Represents an element that is cached in a cache
 * @param <T> the type of element that is cached
 */
public class CachedElement<T extends Serializable> {

    /**
     * The parent cache to cache the element's value
     */
    @NotNull
    private final Cache<T> cache;

    /**
     * The unique id that identifies the cache element in the {@link #cache}
     */
    private final int id;

    /**
     * @param cache sets {@link #cache}
     * @param initialValue the initial value of the allocated cache element
     */
    public CachedElement(@NotNull Cache<T> cache, @Nullable T initialValue) {
        this.cache = cache;
        this.id = cache.allocateElement();
        this.cache.putData(initialValue, id);
    }

    /**
     * Modifies the element's value in the parent {@link #cache}
     * @param value the new element value
     */
    public void set(@Nullable T value){
        this.cache.putData(value, id);
    }

    /**
     * @return the retrieved value from the cache of this element
     */
    @Nullable
    public T retrieveValue(){
        return this.cache.getElement(id).getValue();
    }

    /**
     * Tells the cache that it can optimize the storage of the element's value.
     * Access time to retrieve the element may be longer, if the element is not unoptimized before access.
     * This method call may take some time.
     * @see #unoptimize()
     * @see Cache#optimizeElement(int)
     */
    public void optimize(){
        this.cache.optimizeElement(id);
    }

    /**
     * Tells the cache that the element will now be used frequently and the cache should therefore optimize
     * the element to be quick to access.
     * @see Cache#prepareForUsage(int)
     */
    public void unoptimize(){
        this.cache.prepareForUsage(id);
    }

    public int getId() {
        return id;
    }
}
