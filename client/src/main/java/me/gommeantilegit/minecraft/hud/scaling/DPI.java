package me.gommeantilegit.minecraft.hud.scaling;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

/**
 * Class providing variables for scaling of elements like {@link #dpi}, {@link #scaledWidth}, {@link #scaledHeight} and more.
 * Updated on resize. {@link #update()} is called to update the class variables
 */
public class DPI {

    /**
     * Scaling factor for x and y if desktop
     */
    public static int scaleFactor = 2;

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
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            // Scaled Resolution calculations
            {
                scaledWidth = width;
                scaledHeight = height;
                int scaleFactor = 1;
                while (scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240) {
                    scaleFactor++;
                }
                DPI.scaleFactor = scaleFactor;
                scaledWidth = scaledWidth / scaleFactor;
                scaledHeight = scaledHeight / scaleFactor;
                scaleX = 1f / scaleFactor;
                scaleY = 1f / scaleFactor;
            }
        } else {
            scaledWidth = width * scaleX;
            scaledHeight = height * scaleY;
        }
        scaledWidthi = (int) scaledWidth;
        scaledHeighti = (int) scaledHeight;
    }

    /**
     * @param cursorPosComponent a component of the position vector of a pixel based position on the window
     * @param positionComponent  the index of the position component label (0 --> x, 1 --> y)
     * @return the scaled version of the component by dividing it by the scaleFactor
     */
    public static int getScaledPixelPos(int cursorPosComponent, int positionComponent) {
        return (int) (cursorPosComponent * (positionComponent == 0 ? scaleX : scaleY));
    }
}
