package me.gommeantilegit.minecraft.utils.cache;

import me.gommeantilegit.minecraft.utils.cache.request.result.CacheValueRequestResult;
import me.gommeantilegit.minecraft.utils.cache.request.result.TimeOutCacheValueRequestResult;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Represents a cache that intelligently saves values and sometimes stores them in files.
 *
 * @param <T> the type of values that should be cached
 */
public interface Cache<T extends Serializable> {

    /**
     * @return a unique identifier to identify and find to the new element that was allocated
     */
    int allocateElement();

    /**
     * De-allocates the given element with the specified id from the cache
     * @param id the id of the element to deallocate
     */
    void deallocateElement(int id);

    /**
     * Modifies the data element corresponding to the specified id
     *
     * @param value the new value
     * @param id    the id of the element to be change to the new value
     */
    void putData(@Nullable T value, int id);

    /**
     * @param id the id that the searched element can be found with
     */
    CacheValueRequestResult<T> getElement(int id);

    /**
     * @param id      the id that the searched element can be found with
     * @param timeout a given time that the cache may take to retrieve the value. If this time limit is exceeded, the function will return null.
     */
    @Nullable
    TimeOutCacheValueRequestResult<T> getElement(int id, long timeout);

    /**
     * Tells the cache that the element with the given id will be used in the near future
     *
     * @param id the id of the cached element
     */
    void prepareForUsage(int id);

    /**
     * Tells the cache that the element with the given id will no longer be used.
     * The element will still be available on request, but access time may take longer due to potential optimization of value storage
     *
     * @param id the id of the element
     */
    void optimizeElement(int id);

}
