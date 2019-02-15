package me.gommeantilegit.minecraft.util;

import static com.badlogic.gdx.math.MathUtils.ceil;
import static com.badlogic.gdx.math.MathUtils.floor;
import static com.badlogic.gdx.math.MathUtils.round;

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
        return value > 0 ? ceil(value) : floor(value);
    }

    /**
     * @param value given value
     * @return if (value < 0) floor(value); if(if value >= 0) int(value);
     */
    public static int floorCast(float value) {
        return value > 0 ? (int) value : floor(value);
    }

    /**
     * Converts the given bytes in a human readable string
     *
     * @param bytes the amount of bytes
     * @param si    the unit to use. si = 1000 else 1024 (binary)
     * @return the human readable string
     */
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
}
