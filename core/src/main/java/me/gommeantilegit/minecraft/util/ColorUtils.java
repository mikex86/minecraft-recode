package me.gommeantilegit.minecraft.util;

public class ColorUtils {

    /**
     * Extracts the for RGBA buffers out of the hexadecimal color code and returns the four values in a float array.
     * hex format: 0xaarrggbb
     * Value range: 0..1
     *
     * @param hex the given hexadecimal code
     * @return a float array consisting of the equivalent four rgba values. Value range 0..1
     */
    public static float[] bitShift(long hex) {
        float red = ((hex >> 16) & 0xff) / 255f,
                green = ((hex >> 8) & 0xff) / 255f,
                blue = (hex & 0xff) / 255f,
                alpha = ((hex >> 24) & 0xff) / 255f;
        return new float[]{red, green, blue, alpha};
    }

}
