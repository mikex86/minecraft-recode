package me.gommeantilegit.minecraft.hud.scaling;

import com.badlogic.gdx.Gdx;

public class DPI {

    /**
     * Scaling factor
     */
    public static float scaleFactor = 2f;

    /**
     * Display width and height
     */
    public static int width, height;

    /**
     * Pixel density of the display
     */
    public static float dpi;

    /**
     * The scale-factor as a result of the {@link #dpi} and the screen size. ({@link #width} and {@link #height})
     */
    public static float scaleX, scaleY;

    /**
     * The scaled width and height used for projection and rendering
     */
    public static float scaledWidth, scaledHeight;

    /**
     * Integer versions of {@link #scaledWidth} and {@link #scaledHeight}
     */
    public static int scaledWidthi, scaledHeighti;

    /**
     * Initializes the Helper class
     */
    public static void update() {
        dpi = Gdx.app.getGraphics().getDensity();
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        float scaleValue = ((1 / dpi) * 2) / scaleFactor;
        scaleX = scaleValue;
        scaleY = scaleValue;
        scaledWidth = width * scaleX;
        scaledHeight = height * scaleY;
        scaledWidthi = (int) scaledWidth;
        scaledHeighti = (int) scaledHeight;
    }

}
