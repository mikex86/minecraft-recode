package me.gommeantilegit.minecraft.screenshot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Represents a screen shot
 */
public class ScreenShot {

    private static int counter;

    public ScreenShot(File directory) {

        try {

            FileHandle file;
            while ((file = new FileHandle(directory.getPath() + "/screenshot_" + counter + ".png")).exists()) {
                counter++;
            }

            Pixmap pixmap = getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            PixmapIO.writePNG(file, pixmap);
            pixmap.dispose();

            Gdx.app.log("Screenshot", String.format("Saved screenshot to %s", file.file().getAbsolutePath()));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static Pixmap getFrameBufferPixmap(int x, int y, int width, int height, boolean flipY) {

        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);
        ByteBuffer pixels = pixmap.getPixels();
        Gdx.gl.glReadPixels(x, y, width, height, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);

        int numBytes = width * height * 3;

        byte[] lines = new byte[numBytes];

        if (flipY) {
            int numBytesPerLine = width * 3;

            for (int i = 0; i < height; i++) {
                pixels.position((height - i - 1) * numBytesPerLine);
                pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
            }

            pixels.clear();
            pixels.put(lines);

        } else {
            pixels.clear();
            pixels.get(lines);
        }

        return pixmap;

    }

}
