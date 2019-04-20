package me.gommeantilegit.minecraft.util;

import com.badlogic.gdx.math.Vector3;

public class AngleUtils {

    public static class Angle {
        private final double yaw, pitch;

        public Angle(double yaw, double pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public double getPitch() {
            return pitch;
        }

        public double getYaw() {
            return yaw;
        }
    }

    public static Angle angleTo(Vector3 from, Vector3 to) {
        double xDif = to.x - from.x;
        double yDif = to.y - from.y;
        double zDif = to.z - from.z;
        double distance = from.dst(to);
        return new Angle(Math.toDegrees(Math.atan2(zDif, xDif)), Math.toDegrees(Math.atan2(yDif, distance)));
    }

}
