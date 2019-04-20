package me.gommeantilegit.minecraft.texture.texturewrappers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.gommeantilegit.minecraft.texture.TextureWrapper;
import org.jetbrains.annotations.NotNull;

public class HudTextures extends TextureWrapper {

    /**
     * UV Values for the selection cross [u, v]
     */
    private final int[] selectionCrossUV = new int[]{
            0, 0
    };

    /**
     * Sizes values of the selection cross [width, height]
     */
    private final int[] selectionCrossSizes = new int[]{
            15, 15
    };

    /**
     * UV Values for the heart [u, v]
     */
    private final int[] heartUV = new int[]{
            52, 0
    };

    /**
     * UV Values for the half heart [u, v]
     */
    private final int[] halfHeartUV = new int[]{
            61, 0
    };

    /**
     * UV Values for the white background heart [u, v]
     */
    private final int[] whiteBGHeartUV = new int[]{
            43, 0
    };

    /**
     * UV Values for the black background heart [u, v]
     */
    private final int[] blackBGHeartUV = new int[]{
            16, 0
    };

    /**
     * Sizes values a heart [width, height]
     */
    private final int[] heartSizes = new int[]{
            9, 9
    };

    /**
     * SpriteBatch instance used for 2D rendering
     *
     * @param spriteBatch
     */
    public HudTextures(@NotNull SpriteBatch spriteBatch) {
        //Creating the texture instance and passing it to the super
        super("textures/gui/icons.png", spriteBatch);
    }

    /**
     * Draws a selection cross.
     *
     * @param x    the x coordinate where the method should start drawing the cross
     * @param y    the y coordinate where the method should start drawing the cross
     * @param size the width and height of the cross
     */
    public void drawSelectionCross(int x, int y, int size) {
        drawUVRect(x, y, size, size, selectionCrossUV[0], selectionCrossUV[1], selectionCrossSizes[0], selectionCrossSizes[1], 1, 1, 1, 1, true);
    }

    /**
     * Draws a heart.
     *
     * @param x    the x coordinate where the method should start drawing the heart
     * @param y    the y coordinate where the method should start drawing the heart
     * @param size the width and height of the heart
     */
    public void drawHeart(int x, int y, int size) {
        drawUVSquare(x, y, size, heartUV[0], heartUV[1], heartSizes[0], heartSizes[1], 1, 1, 1, 1, true);
    }

    /**
     * Draws half a heart.
     *
     * @param x    the x coordinate where the method should start drawing the heart
     * @param y    the y coordinate where the method should start drawing the heart
     * @param size the width and height of the heart
     */
    public void drawHalfHeart(int x, int y, int size) {
        drawUVSquare(x, y, size, this.halfHeartUV[0], this.halfHeartUV[1], heartSizes[0], heartSizes[1], 1, 1, 1, 1, true);
    }

    /**
     * Draws a the white background heart.
     *
     * @param x    the x coordinate where the method should start drawing the heart
     * @param y    the y coordinate where the method should start drawing the heart
     * @param size the width and height of the heart
     */
    public void drawWhiteBGHeart(int x, int y, int size) {
        drawUVSquare(x, y, size, this.whiteBGHeartUV[0], this.whiteBGHeartUV[1], heartSizes[0], heartSizes[1], 1, 1, 1, 1, true);
    }

    /**
     * Draws a the black background heart.
     *
     * @param x    the x coordinate where the method should start drawing the heart
     * @param y    the y coordinate where the method should start drawing the heart
     * @param size the width and height of the heart
     */
    public void drawBlackBGHeart(int x, int y, int size) {
        drawUVSquare(x, y, size, this.blackBGHeartUV[0], this.blackBGHeartUV[1], heartSizes[0], heartSizes[1], 1, 1, 1, 1, true);
    }
}
