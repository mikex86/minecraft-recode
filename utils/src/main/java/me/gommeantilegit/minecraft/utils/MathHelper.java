package me.gommeantilegit.minecraft.utils;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

/**
 * Helper class for performing math operations.
 */
public class MathHelper {

    /**
     * the angle is reduced to an angle between -180 and +180 by mod, and a 360 check
     */
    public static float wrapToAngle(float angle) {
        angle = angle % 360.0F;

        if (angle >= 180.0F) {
            angle -= 360.0F;
        }

        if (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    /**
     * @param value given value
     * @return if (value < 0) floor(value); if(if value >= 0) ceil(value);
     */
    public static int floorCeil(float value) {
        return (int) (value > 0 ? ceil(value) : floor(value));
    }

    /**
     * @param value given value
     * @return if (value < 0) floor(value); if(if value >= 0) int(value);
     */
    public static int floorCast(float value) {
        return value > 0 ? (int) value : (int) floor(value);
    }

    /**
     * Converts the given bytes in a human readable string
     *
     * @param bytes the amount of bytes
     * @param si    the unit to use. si = 1000 else 1024 (binary)
     * @return the human readable string
     */
    @SuppressWarnings("Duplicates")
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * the angle is reduced to an angle between -180 and +180 by mod, and a 360 check
     */
    public static float wrapAngleTo180(float value) {
        value = value % 360.0F;
        if (value >= 180.0F) {
            value -= 360.0F;
        }
        if (value < -180.0F) {
            value += 360.0F;
        }
        return value;
    }

    /**
     * @param angle1 the first degree angle
     * @param angle2 the second degree angle
     * @return the degree distance between the two degree angles
     */
    public static double angleDistance(double angle1, double angle2) {
        double phi = Math.abs(angle2 - angle1) % 360;       // This is either the distance or 360 - distance
        return phi > 180 ? 360 - phi : phi;
    }

    /**
     * @param value the given value
     * @return the number of bits needed to represent the given value
     */
    public static int getNeededBits(int value) {
        int count = 0;
        while (value > 0) {
            count++;
            value = value >> 1;
        }
        return count;
    }

    /**
     * @param value the given value
     * @return the number of bits needed to represent the given value
     */
    public static int getNeededBits(long value) {
        int count = 0;
        while (value > 0) {
            count++;
            value = value >> 1;
        }
        return count;
    }

    public static int iceil(int x, int y) {
        return x / y + ((x % y > 0) ? 1 : 0);
    }

    public static int nearestInt(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }

}
