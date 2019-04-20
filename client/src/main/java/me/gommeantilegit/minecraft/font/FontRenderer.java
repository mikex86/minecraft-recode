package me.gommeantilegit.minecraft.font;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import me.gommeantilegit.minecraft.texture.TextureWrapper;

import org.jetbrains.annotations.NotNull;

/**
 * Object for string rendering
 */
public class FontRenderer extends TextureWrapper {

    private final int charSize = 8;
    private final int fontMapSize = 128;
    private final int scaleFactor = 1;
    private final int fontWidth = charSize * scaleFactor;
    public final int fontHeight = charSize * scaleFactor;
    private int charsPerLine = fontMapSize / charSize;
    private final int[] charWidths = new int[256];
    private final int defaultSpacing = 2 * scaleFactor;

    public FontRenderer(@NotNull SpriteBatch spriteBatch) {
        super("font/ascii.png", spriteBatch);
        this.glTexture.getTextureData().prepare();
        initFontWidths();
    }

    /**
     * Reading character pixel widths on texture
     */
    private void initFontWidths() {
        Pixmap pixmap = this.glTexture.getTextureData().consumePixmap();
        for (int code = 0; code < 256; code++) {
            int minX = -1, maxX = -1;
            if (code == ' ') {
                maxX = 2;
                minX = 0;
            } else {
                int pixelX = ((code) % charsPerLine) * charSize;
                int pixelY = ((code) / charsPerLine) * charSize;
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
            }
            int charWidth = maxX - minX + defaultSpacing;
            this.charWidths[code] = charWidth * scaleFactor;
        }
    }

    /**
     * Draws the given string onto the screen.
     *
     * @param string the character to the drawn.
     * @param x      the x coordinate of the top left position of the texture rect with the char applied.
     * @param y      the y coordinate of the top left position of the texture rect with the char applied.
     * @param r      red color component
     * @param g      green color component
     * @param b      blue color component
     * @param a      alpha (transparency) component
     */
    public void drawString(@NotNull String string, float x, float y, float r, float g, float b, float a) {
        this.imageView.spriteBatch.begin();
        char[] chars = string.toCharArray();
        for (char character : chars) {
            drawChar(x, y, character, r, g, b, a);
            x += getCharWidth(character);
        }
        this.imageView.spriteBatch.end();
    }

    /**
     * @see #drawString(String, float, float, float, float, float, float)
     */
    public void drawString(@NotNull String string, float x, float y, @NotNull FontColor fontColor) {
        drawString(string, x, y, fontColor.getR(), fontColor.getG(), fontColor.getB(), 1f);
    }

    /**
     * @param string    the string to draw
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param argbColor the a, r, g, b encoded color code
     * @see #drawString(String, float, float, float, float, float, float)
     */
    public void drawString(@NotNull String string, float x, float y, int argbColor) {
        float r = (float) (argbColor >> 16 & 255) / 255.0F,
                g = (float) (argbColor & 255) / 255.0F,
                b = (float) (argbColor >> 8 & 255) / 255.0F,
                a = (float) (argbColor >> 24 & 255) / 255.0F;
        drawString(string, x, y, r, g, b, a);
    }

    /**
     * Draws the given string and a shadow onto the screen.
     *
     * @param string the character to the drawn.
     * @param x      the x coordinate of the top left position of the texture rect with the char applied.
     * @param y      the y coordinate of the top left position of the texture rect with the char applied.
     * @param r      red color component
     * @param g      green color component
     * @param b      blue color component
     * @param a      alpha (transparency) component
     */
    public void drawStringWithShadow(@NotNull String string, float x, float y, float r, float g, float b, float a) {
        float shadowR, shadowG, shadowB, shadowA;
        // Computing the color of the shadow as efficiently as possible
        {
            int color = (((int) (a * 255 + 0.5) & 0xFF) << 24) | // To convert from normalized 0-1 values efficiently -> ceil(val * 255) is optimized to int(val * 255 + 0.5). Adding +0.5 ceils if cast to an int.
                    (((int) (r * 255 + 0.5) & 0xFF) << 16) |
                    (((int) (g * 255 + 0.5) & 0xFF) << 8) |
                    (((int) (b * 255 + 0.5) & 0xFF));
            if ((color & 0b11111100000000000000000000000000) == 0) {
                color |= 0b11111111000000000000000000000000;
            }
            int l = color & 0xff000000;
            color = (color & 0xfcfcfc) >> 2;
            color += l;
            // Converting ARGB int "shadowColor" back to normalized float color components ranging from 0f to 1f
            shadowR = (float) (color >> 16 & 255) / 255.0F;
            shadowG = (float) (color >> 8 & 255) / 255.0F;
            shadowB = (float) (color & 255) / 255.0F;
            shadowA = (float) (color >> 24 & 255) / 255.0F;
        }
        this.imageView.spriteBatch.begin();
        char[] chars = string.toCharArray();
        for (char character : chars) {
            drawChar(x + scaleFactor, y + scaleFactor, character, shadowR, shadowG, shadowB, shadowA);
            drawChar(x, y, character, r, g, b, a);
            x += getCharWidth(character);
        }
        this.imageView.spriteBatch.end();
    }

    /**
     * @see #drawStringWithShadow(String string, float x, float y, float r, float g, float b, float a)
     */
    public void drawStringWithShadow(@NotNull String string, float x, float y, @NotNull FontColor fontColor) {
        drawStringWithShadow(string, x, y, fontColor.getR(), fontColor.getG(), fontColor.getB(), 1f);
    }

    /**
     * @param string    the string to draw
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param argbColor the a, r, g, b encoded color code
     * @see #drawStringWithShadow(String, float, float, float, float, float, float)
     */
    public void drawStringWithShadow(@NotNull String string, float x, float y, int argbColor) {
        if ((argbColor & 0xfc000000) == 0) {
            argbColor |= 0xff000000; // Giving a color code without alpha an alpha value of 1 (0% transparent)
        }
        float a = (float) (argbColor >> 24 & 255) / 255.0F;
        float r = (float) (argbColor >> 16 & 255) / 255.0F;
        float g = (float) (argbColor >> 8 & 255) / 255.0F;
        float b = (float) (argbColor & 255) / 255.0F;
        drawStringWithShadow(string, x, y, r, g, b, a);
    }

    /**
     * @param character the character whose width should be returned
     * @return the char width of the specified character
     */
    public float getCharWidth(char character) {
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
    private void drawChar(float x, float y, char character, float r, float g, float b, float a) {
        if (character == ' ') {
            return;
        }
        int code = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(character);
        if (code == -1)
            return;
        int tx = (code) % charsPerLine;
        int ty = (code) / charsPerLine;
        drawUVRect(x, y, fontWidth, fontHeight, tx * charSize, ty * charSize, charSize, charSize,
                r,
                g,
                b,
                a,
                false);
    }

    /**
     * @param str the string to be measured
     * @return the font with of the specified string in scaled width units
     */
    public int getStringWidth(@NotNull String str) {
        int width = 0;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            width += getCharWidth(str.charAt(i));
        }
        return width;
    }

    /**
     * Draws a centered string
     *
     * @param string    the string to draw
     * @param x         the x coordinate where the center of the string should be
     * @param y         the y coordinate where the center of the string should be
     * @param argbColor the a r g b encoded color code
     */
    public void drawCenteredStringWithShadow(@NotNull String string, float x, float y, int argbColor) {
        drawStringWithShadow(string, x - getStringWidth(string) / 2f, y, argbColor);
    }
}
