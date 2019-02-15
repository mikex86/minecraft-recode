package me.gommeantilegit.minecraft.util.math.vecmath.intvectors;

import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.atan2;
import static java.lang.Math.hypot;
import static java.lang.Math.toDegrees;

/**
 * Object for representing two dimensional positional data
 */
public class Vec2i {

    /**
     * Two dimensional position values
     */
    protected int x, y;

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
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
}
