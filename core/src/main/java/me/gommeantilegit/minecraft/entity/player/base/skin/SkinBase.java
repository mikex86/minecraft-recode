package me.gommeantilegit.minecraft.entity.player.base.skin;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a minecraft skin (an image -> array of pixel)
 */
public class SkinBase {

    /**
     * Pixel array to represent the texture.
     * pixels[x][y] -> Pixel
     */
    @NotNull
    private final SkinPixel[][] pixels;

    /**
     * @param pixels rectangle shaped pixel array
     * @throws AssertionError if array shape is not rectangular
     */
    public SkinBase(@NotNull SkinPixel[][] pixels) {
        this.pixels = pixels;
        int len = pixels[0].length;
        for (SkinPixel[] p : pixels)
            assert p.length == len;
    }

    @NotNull
    public SkinPixel[][] getPixels() {
        return pixels;
    }
}

