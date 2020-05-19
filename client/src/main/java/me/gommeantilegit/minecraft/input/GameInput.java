package me.gommeantilegit.minecraft.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import me.gommeantilegit.minecraft.screenshot.ScreenShot;

import java.io.File;

public class GameInput extends InputAdapter {

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode){
            case Input.Keys.F11:
                new ScreenShot(new File("screenshots"));
                break;
        }
        return super.keyDown(keycode);
    }
}
