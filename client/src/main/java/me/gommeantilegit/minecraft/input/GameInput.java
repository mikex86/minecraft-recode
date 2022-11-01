package me.gommeantilegit.minecraft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.screenshot.ScreenShot;

import java.io.File;

public class GameInput extends InputAdapter {

    private final ClientMinecraft mc;

    public GameInput(ClientMinecraft mc) {
        this.mc = mc;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == mc.gameSettings.keyBindings.keyBindScreenshot.getValue()) {
            new ScreenShot(new File("screenshots"));
        } else if (keycode == mc.gameSettings.keyBindings.keyBindFullscreen.getValue()) {
            boolean previouslyInFullscreen = mc.gameSettings.videoSettings.fullscreen.getValue();
            mc.gameSettings.videoSettings.fullscreen.setValue(!previouslyInFullscreen);

            if (previouslyInFullscreen) {
                Gdx.graphics.setWindowedMode(854, 480);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }
        return super.keyDown(keycode);
    }
}
