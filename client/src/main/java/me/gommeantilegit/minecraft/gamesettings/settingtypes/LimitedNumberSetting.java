package me.gommeantilegit.minecraft.gamesettings.settingtypes;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a setting that can have numeric values in a given predefined interval
 */
public class LimitedNumberSetting extends NumberSetting {

    /**
     * The interval that the limited number's value must be in
     */
    @NotNull
    private final Interval interval;

    /**
     * @param name              sets {@link #name}
     * @param integerValuesOnly sets {@link #integerValuesOnly}
     * @param interval          sets {@link #interval}
     */
    public LimitedNumberSetting(@NotNull String name, boolean integerValuesOnly, @NotNull Interval interval) {
        super(name, integerValuesOnly);
        this.interval = interval;
    }

    @Override
    public LimitedNumberSetting setValue(Double value) {
        if (interval.isContained(value))
            return (LimitedNumberSetting) super.setValue(value);
        else
            throw new IllegalArgumentException("Cannot set LimitedNumberSetting's value to " + value + " as it is not contained in the given interval: " + interval);
    }

    @Override
    public LimitedNumberSetting addValueChangedListener(@NotNull SettingValueChangedListener<Double> changedListener) {
        return (LimitedNumberSetting) super.addValueChangedListener(changedListener);
    }

    @NotNull
    public Interval getInterval() {
        return interval;
    }

    /**
     * Represents an inclusive interval
     */
    public static class Interval {

        /**
         * Minimum and maximum values of the interval
         */
        private final double min, max;

        public Interval(double min, double max) {
            this.min = min;
            this.max = max;
        }

        /**
         * @param x a given value x
         * @return true, if the given value x is >= {@link #min} and <= {@link #max}
         */
        public boolean isContained(double x) {
            return x >= min && x <= max;
        }

        public double getMax() {
            return max;
        }

        public double getMin() {
            return min;
        }

        @Override
        public String toString() {
            return "{min = " + min + ", max =" + max + "}";
        }

        /**
         * @param x the value that the relative value should be returned from
         * @return a relative representation of x in the interval [0; 1] scaling to [min; max]
         */
        public double getRelativeValue(double x) {
            return (x - min) / (max - min);
        }
    }
}
