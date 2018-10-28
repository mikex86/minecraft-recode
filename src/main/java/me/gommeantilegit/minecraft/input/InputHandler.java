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
        inputProcessors.forEach(p -> p.keyDown(keycode));
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        inputProcessors.forEach(p -> p.keyUp(keycode));
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        inputProcessors.forEach(p -> p.keyTyped(character));
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        inputProcessors.forEach(p -> p.touchDown(screenX, screenY, pointer, button));
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        inputProcessors.forEach(p -> p.touchUp(screenX, screenY, pointer, button));
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        inputProcessors.forEach(p -> p.touchDragged(screenX, screenY, pointer));
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        inputProcessors.forEach(p -> p.mouseMoved(screenX, screenY));
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        inputProcessors.forEach(p -> p.scrolled(amount));
        return true;
    }
}
