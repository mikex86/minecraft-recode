package me.gommeantilegit.minecraft.font;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

public enum FontColor {
    BLACK(0),
    DARK_BLUE(1),
    DARK_GREEN(2),
    DARK_AQUA(3),
    DARK_RED(4),
    DARK_PURPLE(5),
    GOLD(6),
    GRAY(7),
    DARK_GRAY(8),
    BLUE(9),
    GREEN(10),
    AQUA(11),
    RED(12),
    LIGHT_PURPLE(13),
    YELLOW(14),
    WHITE(15);

    @NotNull
    private static final String str = "0123456789abcdef";

    /**
     * The rgba color code
     */
    private final int colorCode;

    /**
     * Normalized (0..1) R, G, B values
     */
    private final float r, g, b;

    /**
     * @param index the index of the font color ranging from 0-15
     */
    FontColor(int index) {
        int j = (index >> 3 & 1) * 85;
        int k = (index >> 2 & 1) * 170 + j;
        int l = (index >> 1 & 1) * 170 + j;
        int i1 = (index & 1) * 170 + j;
        if (index == 6) {
            k += 85;
        }
        if (index >= 16) {
            k /= 4;
            l /= 4;
            i1 /= 4;
        }
        this.colorCode = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        Color color = new Color(colorCode);
        this.r = color.getRed() / 255f;
        this.g = color.getGreen() / 255f;
        this.b = color.getBlue() / 255f;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public int getColorCode() {
        return colorCode;
    }
}
