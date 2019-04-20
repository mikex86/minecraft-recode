package me.gommeantilegit.minecraft.utils.cache.request.result;

/**
 * Represents the result of request a preformed on a cache to retrieve the value of a given element, where the cache has a given timeout find that value.
 * This result also stores information whether the request expired or not.
 * @param <T> the type of element requested
 */
public class TimeOutCacheValueRequestResult<T> extends CacheValueRequestResult<T>{

    /**
     * State whether or not the request was fulfilled by the cache in time.
     */
    private final boolean timeOutMet;

    /**
     * @param value sets {@link #value}
     * @param valueFound sets {@link #valueFound}
     * @param timeOutMet sets {@link #timeOutMet}
     */
    public TimeOutCacheValueRequestResult(T value, boolean valueFound, boolean timeOutMet) {
        super(value, valueFound);
        this.timeOutMet = timeOutMet;
    }

    /**
     * @return true if {@link #valueFound} and {@link #timeOutMet} are both true.
     */
    @Override
    public boolean isValuePresent() {
        return super.isValuePresent() && timeOutMet;
    }

    /**
     * @return {@link #timeOutMet}
     */
    public boolean wasTimeOutMet() {
        return timeOutMet;
    }
}
