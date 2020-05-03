package me.gommeantilegit.minecraft.util.math.vecmath.intvectors;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.utils.collections.LongHashable;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.atan2;
import static java.lang.Math.hypot;
import static java.lang.Math.toDegrees;

/**
 * Object for representing two dimensional positional data
 */
public class Vec2i implements LongHashable {

    /**
     * Two dimensional position values
     */
    protected int x, y;

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Casts down the float x, y component of the specified float vector to the next integer (floor)
     *
     * @param vector the specified vector
     */
    public Vec2i(@NotNull Vector2 vector) {
        this((int) vector.x, (int) vector.y);
    }

    public static long hash64(int x, int y) {
        return ((long) (x) & 0xffffffffL) | (((long) y & 0xffffffffL) << 32);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getDistance(Vec2i point) {
        return hypot(point.x - x, point.y - y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vec2i)
            return ((Vec2i) obj).x == x && ((Vec2i) obj).y == y;
        else return false;
    }

    /**
     * @param vec2i the given point.
     * @return the angle to the given point.
     */
    public double angleTo(Vec2i vec2i) {
        return toDegrees(atan2((y - vec2i.y), (x - vec2i.x)));
    }

    /**
     * @return the libGDX {@link Vector2} value equivalent of this vector
     */
    @NotNull
    public Vector2 asLibGDXVec2D() {
        return new Vector2(x, y);
    }

    @Override
    public long hash64() {
        return hash64(x, y);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hash64());
    }

    @Override
    public String toString() {
        return "Vec2i{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
