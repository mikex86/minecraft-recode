package me.gommeantilegit.minecraft.util;

public class Clock {

    /**
     * Whether or not the clock resolution should be nanoseconds.
     * true = nanoseconds
     * false = milliseconds
     */
    private boolean nanoseconds;

    /**
     * Time the clock was reset the last time.
     */
    private long lastReset;

    /**
     * {@link #lastReset} is set to {@link #getCurrentTime()}
     *
     * @param nanoseconds initializes {@link #nanoseconds}
     */
    public Clock(boolean nanoseconds) {
        this.nanoseconds = nanoseconds;
        reset();
    }

    /**
     * @return the current time in either nano
     */
    private long getCurrentTime() {
        return nanoseconds ? System.nanoTime() : System.currentTimeMillis();
    }

    public void reset() {
        this.lastReset = getCurrentTime();
    }

    /**
     * @return the time passed since last call of {@link #reset()}
     * in nanoseconds or milliseconds depending on the state of {@link #nanoseconds}
     */
    public long getTimePassed() {
        return getCurrentTime() - lastReset;
    }

    public boolean isNanoseconds() {
        return nanoseconds;
    }
}