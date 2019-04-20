package me.gommeantilegit.minecraft.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VectorUtils {

    /**
     * @param source the source vector
     * @param target the target vector
     * @param x      the x coordinate of the vector returned
     * @return a new vector with x value equal to the third parameter, along the line between the source vector and the
     * passed in vector, or null if not possible.
     */
    @Nullable
    public static Vector3 getIntermediateWithXValue(@NotNull Vector3 source, @NotNull Vector3 target, float x) {
        float d0 = target.x - source.x;
        float d1 = target.y - source.y;
        float d2 = target.z - source.z;

        if (d0 * d0 < 1.0000000116860974E-7D) {
            return null;
        } else {
            float d3 = (x - target.x) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3(target.x + d0 * d3, target.y + d1 * d3, target.z + d2 * d3) : null;
        }
    }

    /**
     * @param source the source vector
     * @param target the target vector
     * @param y      the y coordinate of the vector returned
     * @return a new vector with y value equal to the second parameter, along the line between the source vector and the
     * passed in vector, or null if not possible.
     */
    public static Vector3 getIntermediateWithYValue(@NotNull Vector3 source, @NotNull Vector3 target, float y) {
        float d0 = target.x - source.x;
        float d1 = target.y - source.y;
        float d2 = target.z - source.z;

        if (d1 * d1 < 1.0000000116860974E-7D) {
            return null;
        } else {
            float d3 = (y - source.y) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3(source.x + d0 * d3, source.y + d1 * d3, source.z + d2 * d3) : null;
        }
    }

    /**
     * @param source the source vector
     * @param target the target vector
     * @param z      the z coordinate of the vector returned
     * @return a new vector with z value equal to the second parameter, along the line between the source vector and the
     * passed in vector, or null if not possible.
     */
    public static Vector3 getIntermediateWithZValue(@NotNull Vector3 source, @NotNull Vector3 target, float z) {
        float d0 = target.x - source.x;
        float d1 = target.y - source.y;
        float d2 = target.z - source.z;

        if (d2 * d2 < 1.0000000116860974E-7D) {
            return null;
        } else {
            float d3 = (z - source.z) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3(source.x + d0 * d3, source.y + d1 * d3, source.z + d2 * d3) : null;
        }
    }

    /**
     * @param vec3 the 3d vector
     * @return a 2D vector constructed from the x and z components of the specified vector
     */
    @NotNull
    public static Vector2 xzTo2D(@NotNull Vector3 vec3) {
        return new Vector2(vec3.x, vec3.z);
    }
}
