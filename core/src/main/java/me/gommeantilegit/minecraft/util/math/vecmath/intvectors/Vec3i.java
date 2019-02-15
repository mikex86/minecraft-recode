package me.gommeantilegit.minecraft.util.math.vecmath.intvectors;

import com.badlogic.gdx.math.Vector3;

import java.util.Collection;

/**
 * Object representing a position in 3D space
 */
public class Vec3i extends Vec2i {

    public static final Vec3i NULL = new Vec3i(0, 0, 0);
    /**
     * Third dimension
     */
    private int z;

    /**
     * @param x initializes {@link #x}
     * @param y initializes {@link #y}
     * @param z initializes {@link #z}
     */
    public Vec3i(int x, int y, int z) {
        super(x, y);
        this.z = z;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    /**
     * @return the {@link Vector3} equivalent of this vector
     */
    public Vector3 asLibGDXVec3D() {
        return new Vector3(x, y, z);
    }
}
