package me.gommeantilegit.minecraft.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.jetbrains.annotations.NotNull;

import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.texture.rendering.ImageView;

/**
 * Represents a class capable of drawing certain objects from a texture by drawing different texture regions.
 */
public class TextureWrapper {

    /**
     * The texture that should be drawn. Initialized by the textureResource String parameter of the constructor {@link #TextureWrapper(String, SpriteBatch)}
     */
    @NotNull
    protected final CustomTexture glTexture;

    /**
     * Image view instance for rendering {@link #glTexture} in 2D.
     */
    @NotNull
    public final ImageView imageView;

    /**
     * @param textureResource the class path to the image resource to be drawn.
     * @param spriteBatch     the spriteBatch instance used for 2D rendering.
     */
    public TextureWrapper(@NotNull String textureResource, @NotNull SpriteBatch spriteBatch) {
        this.glTexture = new CustomTexture(Gdx.files.classpath(textureResource));
        this.imageView = new ImageView(glTexture, spriteBatch);
    }

    /**
     * @param texture     the texture that the wrapper should handle
     * @param spriteBatch the sprite batch instance used for 2d rendering.
     */
    public TextureWrapper(@NotNull CustomTexture texture, @NotNull SpriteBatch spriteBatch) {
        this.glTexture = texture;
        this.imageView = new ImageView(glTexture, spriteBatch);
    }

    /**
     * Draws a textured rectangle with {@link #glTexture} applied to it.
     *
     * @param x           the top left x position of the rectangle
     * @param y           the top left y position of the rectangle
     * @param width       the width of the rectangle
     * @param height      the height of the rectangle
     * @param pixelX      the x pixel coordinate where the texture region to be drawn starts.
     * @param pixelY      the y pixel coordinate where the texture region to be drawn starts.
     * @param pixelWidth  the width of the region of the texture to be drawn.
     * @param pixelHeight the height of the region of the texture to be drawn.
     * @param r           the red component of the color that is applied to the texture being rendered.
     * @param g           the green component of the color that is applied to the texture being rendered.
     * @param b           the blue component of the color that is applied to the texture being rendered.
     * @param a           the alpha (transparency) component of the color that is applied to the texture being rendered.
     * @param flush       the state whether the spritebatch should start and stop drawing before / after the render calls
     */
    public void drawUVRect(float x, float y, float width, float height, int pixelX, int pixelY, int pixelWidth, int pixelHeight,
                           float r, float g, float b, float a, boolean flush) {
        this.imageView.drawUVTexturedRect(x, y, width, height,
                (float) pixelX / glTexture.getWidth(), (float) pixelY / glTexture.getHeight(),
                (float) pixelWidth / glTexture.getWidth(), (float) pixelHeight / glTexture.getHeight(), r, g, b, a, flush);
    }

    /**
     * Draws a texture square with {@link #glTexture} applied to it to the screen.
     *
     * @param x           the top left x position of the square
     * @param y           the top left y position of the square
     * @param size        size of the square. Width equals height.
     * @param pixelX      the x pixel coordinate where the texture region to be drawn starts.
     * @param pixelY      the y pixel coordinate where the texture region to be drawn starts.
     * @param pixelWidth  the width of the region of the texture to be drawn.
     * @param pixelHeight the height of the region of the texture to be drawn.
     * @param r           red component of the color that is applied to the texture being rendered.
     * @param g           green component of the color that is applied to the texture being rendered.
     * @param b           blue component of the color that is applied to the texture being rendered.
     * @param a           alpha (transparency) component of the color that is applied to the texture being rendered.
     * @param flush       the state whether the spritebatch should start and stop drawing before / after the render calls
     */
    public void drawUVSquare(float x, float y, float size, int pixelX, int pixelY, int pixelWidth, int pixelHeight, float r, float g, float b, float a, boolean flush) {
        drawUVRect(x, y, size, size, pixelX, pixelY, pixelWidth, pixelHeight, r, g, b, a, flush);
    }

    /**
     * Draws a textured rectangle with {@link #glTexture} applied to it.
     *
     * @param x          the top left x position of the rectangle
     * @param y          the top left y position of the rectangle
     * @param width      the width of the rectangle
     * @param height     the height of the rectangle
     * @param renderData an object combining parameters pixelPositions and pixelSizes representing a texture region.
     * @param r          red component of the color that is applied to the texture being rendered.
     * @param g          green component of the color that is applied to the texture being rendered.
     * @param b          blue component of the color that is applied to the texture being rendered.
     * @param a          alpha (transparency) component of the color that is applied to the texture being rendered.
     * @param flush      the state whether the spritebatch should start and stop drawing before / after the render calls
     * @see #drawUVRect(float, float, float, float, int, int, int, int, float, float, float, float, boolean)
     */
    public void drawUVRect(float x, float y, float width, float height, @NotNull RenderData renderData, float r, float g, float b, float a, boolean flush) {
        this.imageView.drawUVTexturedRect(x, y, width, height,
                (float) renderData.getPixelPositions()[0] / glTexture.getWidth(), (float) renderData.getPixelPositions()[1] / glTexture.getHeight(),
                (float) renderData.getPixelSizes()[0] / glTexture.getWidth(), (float) renderData.getPixelSizes()[1] / glTexture.getHeight(), r, g, b, a, flush);
    }

    /**
     * Object representing a texture region (data un-normalized and pixel dependent)
     */
    public static class RenderData {


        /**
         * The int[] storing the x and y pixels where the texture region to be drawn starts.
         */
        private final int[] pixelPositions;
        /**
         * The int[] storing width and height of the region of the texture to be drawn.
         */
        private final int[] pixelSizes;

        /**
         * @param pixelPositions the int[] storing the x and y pixels where the texture region to be drawn starts.
         * @param pixelSizes     the int[] storing width and height of the region of the texture to be drawn.
         */
        public RenderData(int[] pixelPositions, int[] pixelSizes) {
            this.pixelPositions = pixelPositions;
            this.pixelSizes = pixelSizes;
        }

        public int[] getPixelPositions() {
            return pixelPositions;
        }

        public int[] getPixelSizes() {
            return pixelSizes;
        }
    }

    @NotNull
    public CustomTexture getGlTexture() {
        return glTexture;
    }
}
