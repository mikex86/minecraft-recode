package me.gommeantilegit.minecraft.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import org.jetbrains.annotations.NotNull;

public class VectorUtils {

    /**
     * @param vec3 the 3d vector
     * @return a 2D vector constructed from the x and z components of the specified vector
     */
    @NotNull
    public static Vector2 xzTo2D(@NotNull Vector3 vec3) {
        return new Vector2(vec3.x, vec3.z);
    }

    /**
     * Casts the given float 3d vector to a 2d int vector of (x, z)
     * @param vec3 the 3d vector
     * @return a 2D vector constructed from the x and z components of the specified vector
     */
    @NotNull
    public static Vec2i xzTo2Di(@NotNull Vector3 vec3) {
        return new Vec2i((int) vec3.x, (int) vec3.z);
    }
}
