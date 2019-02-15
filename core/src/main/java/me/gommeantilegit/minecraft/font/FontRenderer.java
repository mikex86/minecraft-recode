package me.gommeantilegit.minecraft.font;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import me.gommeantilegit.minecraft.texture.TextureWrapper;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.util.ColorUtils;

import org.jetbrains.annotations.NotNull;

public class FontRenderer extends TextureWrapper {

    private final int charSize = 8;
    private final int fontMapSize = 128;
    private final int scaleFactor = 3;
    private final int fontWidth = charSize * scaleFactor, fontHeight = charSize * scaleFactor;
    private int charsPerLine = fontMapSize / charSize;
    private final int[] charWidths = new int[256];
    private final float defaultSpacing = 4;

    public FontRenderer(@NotNull SpriteBatch spriteBatch) {
        super("font/ascii.png", spriteBatch);
        this.glTexture.getTextureData().prepare();
        initFontWidths();
    }

    private void initFontWidths() {
        Pixmap pixmap = this.glTexture.getTextureData().consumePixmap();
        for (int code = 0; code < 256; code++) {
            int pixelX = ((code) % charsPerLine) * charSize;
            int pixelY = ((code) / charsPerLine) * charSize;
            int minX = -1, maxX = -1;
            for (int addY = 0; addY < charSize; addY++) {
                for (int addX = 0; addX < charSize; addX++) {
                    int xPix = pixelX + addX;
                    int yPix = pixelY + addY;
                    int rgb = pixmap.getPixel(xPix, yPix);
                    if (rgb == 0xffffffff) {
                        if (minX == -1 || addX < minX) {
                            minX = addX;
                        }
                        if (maxX == -1 || addX > maxX) {
                            maxX = addX;
                        }
                    }
                }
            }
            int charWidth = maxX - minX + 1;
            this.charWidths[code] = charWidth * scaleFactor;
        }
    }
    /**
     * Draws the given string onto the screen.
     * @param string the character to the drawn.
     * @param x         the x coordinate of the top left position of the texture rect with the char applied.
     * @param y         the y coordinate of the top left position of the texture rect with the char applied.
     * @param r         red color component
     * @param g         green color component
     * @param b         blue color component
     * @param a         alpha (transparency) component
     */
    public void drawString(String string, float x, float y, float r, float g, float b, float a) {
        char[] chars = string.toCharArray();
        for (char character : chars) {
            drawChar(x, y, character, r, g, b, a);
            x += getWidth(character) + defaultSpacing;
        }
    }

    private float getWidth(char character) {
        return charWidths[character];
    }

    /**
     * Draws the given character onto the screen.
     *
     * @param x         the x coordinate of the top left position of the texture rect with the char applied.
     * @param y         the y coordinate of the top left position of the texture rect with the char applied.
     * @param character the character to the drawn.
     * @param r         red color component
     * @param g         green color component
     * @param b         blue color component
     * @param a         alpha (transparency) component
     */
    public void drawChar(float x, float y, char character, float r, float g, float b, float a) {
        int code = (int) character;
        int tx = (code) % charsPerLine;
        int ty = (code) / charsPerLine;
        drawUVRect(x, y, fontWidth, fontHeight, new int[]{tx * charSize, ty * charSize}, new int[]{charSize, charSize},
                r,
                g,
                b,
                a);
    }
}
