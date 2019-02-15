package me.gommeantilegit.minecraft.timer;

import me.gommeantilegit.minecraft.timer.api.Tickable;

public class Timer implements Tickable {

//    private static final long NS_PER_SECOND = 1000000000L;
//    private static final long MAX_NS_PER_UPDATE = 1000000000L;
//    private static final int MAX_TICKS_PER_UPDATE = 100;
    private float ticksPerSecond;
    private long lastTime;
    public int ticks;
    public long performedTicks;
    public float partialTicks;
    public float timeScale = 1.0f;
    public float passedTime = 0.0f;

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
        this.passedTime += (float) passedNs * this.timeScale * this.ticksPerSecond / 1.0E9f;
        this.ticks = (int) this.passedTime;
        if (this.ticks > 100) {
            this.ticks = 100;
        }
        this.passedTime -= (float) this.ticks;
        this.partialTicks = this.passedTime;
    }

    public void tick(float partialTicks) {
        performedTicks++;
    }
}

