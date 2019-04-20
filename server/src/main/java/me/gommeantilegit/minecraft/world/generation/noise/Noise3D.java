package me.gommeantilegit.minecraft.world.generation.noise;

public interface Noise3D {

    /**
     * @param x x coordinate
     * @param y y coordinate
     * @return the calculated noise value for the specified coordinates
     */
    default double getNoise(double x, double y) {
        return getNoise(x, y, 1);
    }

    /**
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return the calculated noise value for the specified coordinates
     */
    double getNoise(double x, double y, double z);

}
