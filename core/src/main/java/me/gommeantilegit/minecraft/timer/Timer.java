package me.gommeantilegit.minecraft.timer;

import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.utils.data.DataCollector;
import me.gommeantilegit.minecraft.utils.stream.streamer.impl.FloatDataStreamer;
import org.jetbrains.annotations.NotNull;

/**
 * Object for timing the minecraft tick
 */
public class Timer implements Tickable {

    /**
     * Ticks per second / TPS rate (eg. 20TPS = invoking game tick 20 times per seconds)
     */
    private final float ticksPerSecond;


    private long lastTime;

    /**
     * Amount of ticks to perform
     */
    public int ticks;

    /**
     * Amount of ticks already performed
     */
    public long performedTicks;

    /**
     * Relative time between ticks
     */
    public float partialTicks;

    /**
     * Timer speed value to relatively change the TPS rate
     * timerSpeed 1 is normal speed -> 20TPS
     */
    private float timerSpeed = 1.0f;

    /**
     * Current amount of TPS (Ticks per second)
     */
    private float currentTicksPerSecond;

    /**
     * Ticks performed in the current second
     */
    private int secondTicks = 0;

    /**
     * MS resolution timestamp to compute {@link #currentTicksPerSecond}
     */
    private long secondTimer = System.currentTimeMillis();

    /**
     * Pre value of {@link #ticks}
     * {@link #ticks} cannot be greater than 100. If the game runs to slowly, the ticks that need to be performed
     * because of the lag are sometimes greater than 100 meaning the lag took longer than 5 seconds assuming {@link #ticksPerSecond} is
     * at its default rate 20 TPS
     * The ticks that thus need to be performed afterwards are stored in this variable
     * As only 100 ticks per call of {@link #advanceTime()} can be scheduled on the minecraft instance, this variable is decrementing
     * as they are performed
     */
    private float ticksLeft = 0.0f;

    /**
     * Collects tps data
     */
    @NotNull
    private final DataCollector<Float> tpsDataCollector = new DataCollector<>(new FloatDataStreamer());

    public Timer(float ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
        this.lastTime = System.nanoTime();
    }

    public void advanceTime() {
        long now = System.nanoTime();
        long passedNs = now - this.lastTime;
        this.lastTime = now;
        if (passedNs < 0L) {
            passedNs = 0L;
        }
        if (passedNs > 1000000000L) {
            passedNs = 1000000000L;
        }
        this.ticksLeft += (float) passedNs * this.timerSpeed * this.ticksPerSecond / 1.0E9f;
        this.ticks = (int) this.ticksLeft;
        if (this.ticks > 100) {
            this.ticks = 100;
        }
        this.ticksLeft -= (float) this.ticks;
        this.partialTicks = this.ticksLeft;
    }

    public void tick(float partialTicks) {
        this.performedTicks++;
        this.secondTicks++;
        long now = System.currentTimeMillis();
        if (now - secondTimer > 1000) {
            currentTicksPerSecond = secondTicks / ((now - secondTimer) / 1000.0f);
            if (currentTicksPerSecond < ticksPerSecond - 1)
                System.out.println("TPS dropped to " + currentTicksPerSecond + "!");
            this.secondTicks = 0;
            this.secondTimer = now;
            this.tpsDataCollector.collectAsync(getCurrentTicksPerSecond());
        }
    }

    public float getCurrentTicksPerSecond() {
        return currentTicksPerSecond;
    }

    public float getTicksPerSecond() {
        return ticksPerSecond;
    }

    @NotNull
    public DataCollector<Float> getTpsDataCollector() {
        return tpsDataCollector;
    }
}

