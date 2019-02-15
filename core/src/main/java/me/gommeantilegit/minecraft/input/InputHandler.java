package me.gommeantilegit.minecraft.input;

import com.badlogic.gdx.InputProcessor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class InputHandler implements InputProcessor {

    @NotNull
    private final ArrayList<InputProcessor> inputProcessors = new ArrayList<>();

    public void registerInputProcessor(InputProcessor processor) {
        this.inputProcessors.add(processor);
    }

    @NotNull
    public ArrayList<InputProcessor> getInputProcessors() {
        return inputProcessors;
    }

    @Override
    public boolean keyDown(int keycode) {
        for (InputProcessor p : inputProcessors) {
            p.keyDown(keycode);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        for (InputProcessor p : inputProcessors) {
            p.keyUp(keycode);
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        for (InputProcessor p : inputProcessors) {
            p.keyTyped(character);
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (InputProcessor inputProcessor : inputProcessors) {
            inputProcessor.touchDown(screenX, screenY, pointer, button);
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (InputProcessor p : inputProcessors) {
            p.touchUp(screenX, screenY, pointer, button);
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        for (InputProcessor p : inputProcessors) {
            p.touchDragged(screenX, screenY, pointer);
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        for (InputProcessor p : inputProcessors) {
            p.mouseMoved(screenX, screenY);
        }
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        for (InputProcessor p : inputProcessors) {
            p.scrolled(amount);
        }
        return true;
    }
}
