package me.gommeantilegit.minecraft.entity.player.skin;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import me.gommeantilegit.minecraft.entity.player.base.skin.SkinBase;
import me.gommeantilegit.minecraft.entity.player.base.skin.SkinPixel;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Client side skin implementation.
 * Stores a texture of {@link #pixels}
 */
public class ClientSkin extends SkinBase {

    /**
     * The skin texture
     */
    @NotNull
    private final CustomTexture texture;

    /**
     * @param pixels rectangle shaped pixel array
     * @throws AssertionError if array shape is not rectangular
     */
    public ClientSkin(@NotNull SkinPixel[][] pixels) {
        super(pixels);
        Pixmap pixmap = new Pixmap(pixels.length, pixels[0].length, Pixmap.Format.RGBA8888);
        for (int x = 0; x < pixmap.getWidth(); x++) {
            for (int y = 0; y < pixmap.getHeight(); y++) {
                SkinPixel pixel = pixels[x][y];
                // Pixel r, g, b, a unsigned bytes are converted to integers
                Color color = new Color(pixel.getR() & 0xFF, pixel.getG() & 0xFF, pixel.getB() & 0xFF, pixel.getA() & 0xFF);
                pixmap.drawPixel(x, y, color.getRGB());
            }
        }
        this.texture = new CustomTexture(pixmap);
    }

    /**
     * @param pixmap specified Pixmap being the skin image
     * @throws AssertionError if array shape is not rectangular
     */
    public ClientSkin(@NotNull Pixmap pixmap) {
        super(toPixelArray(pixmap));
        this.texture = new CustomTexture(pixmap);
    }

    @NotNull
    private static SkinPixel[][] toPixelArray(@NotNull Pixmap pixmap) {
        int width = pixmap.getWidth(), height = pixmap.getHeight();
        SkinPixel[][] pixels = new SkinPixel[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = pixmap.getPixel(x, y);
                Color color = new Color(rgb);
                // Converting 0..255 int to -127..127 byte (treated though as unsigned byte)
                byte r = (byte) color.getRed(), g = (byte) color.getGreen(), b = (byte) color.getBlue(), a = (byte) color.getAlpha();
                pixels[x][y] = new SkinPixel(r, g, b, a);
            }
        }
        return pixels;
    }


    public ClientSkin(@NotNull FileHandle fileHandle) {
        this(toPixmap(fileHandle));
    }

    /**
     * Reads the specified image and converts it into a Pixmap of 8 bit color depth
     *
     * @param fileHandle the file handle
     * @return a Pixmap of 8 bit color depth
     */
    @NotNull
    private static Pixmap toPixmap(@NotNull FileHandle fileHandle) {
        return new Pixmap(fileHandle);
    }

    @NotNull
    public CustomTexture getTexture() {
        return texture;
    }
}
