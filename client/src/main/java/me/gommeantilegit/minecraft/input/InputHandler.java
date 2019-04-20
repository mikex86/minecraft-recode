package me.gommeantilegit.minecraft.input;

import com.badlogic.gdx.InputProcessor;

import me.gommeantilegit.minecraft.ui.screen.GuiScreen;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Object handling input processors
 */
public class InputHandler implements InputProcessor {

    /**
     * List of all registered input processor that should be invoked on input
     */
    @NotNull
    private final ArrayList<InputProcessor> inputProcessors = new ArrayList<>();

    /**
     * Registers an additional processor that should be invoked on input
     *
     * @param processor the specified input processor to be registered
     */
    public void registerInputProcessor(InputProcessor processor) {
        synchronized (inputProcessors) {
            this.inputProcessors.add(processor);
        }
    }

    /**
     * Unregisters the specified input processor. It is no longer invoked on input
     *
     * @param processor the specified input processor to be unregistered
     */
    public void unregisterProcessor(@NotNull InputProcessor processor) {
        synchronized (inputProcessors) {
            this.inputProcessors.remove(processor);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        synchronized (inputProcessors) {
            for (int i = 0; i < inputProcessors.size(); i++) {
                InputProcessor p = inputProcessors.get(i);
                p.keyDown(keycode);
            }
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        synchronized (inputProcessors) {
            for (int i = 0; i < inputProcessors.size(); i++) {
                InputProcessor p = inputProcessors.get(i);
                p.keyUp(keycode);
            }
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        synchronized (inputProcessors) {
            for (int i = 0; i < inputProcessors.size(); i++) {
                InputProcessor p = inputProcessors.get(i);
                p.keyTyped(character);
            }
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        synchronized (inputProcessors) {
            for (int i = 0; i < inputProcessors.size(); i++) {
                InputProcessor inputProcessor = inputProcessors.get(i);
                inputProcessor.touchDown(screenX, screenY, pointer, button);
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        synchronized (inputProcessors) {
            for (int i = 0; i < inputProcessors.size(); i++) {
                InputProcessor p = inputProcessors.get(i);
                p.touchUp(screenX, screenY, pointer, button);
            }
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        synchronized (inputProcessors) {
            for (int i = 0; i < inputProcessors.size(); i++) {
                InputProcessor p = inputProcessors.get(i);
                p.touchDragged(screenX, screenY, pointer);
            }
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        synchronized (inputProcessors) {
            for (int i = 0; i < inputProcessors.size(); i++) {
                InputProcessor p = inputProcessors.get(i);
                p.mouseMoved(screenX, screenY);
            }
        }
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        synchronized (inputProcessors) {
            for (int i = 0; i < inputProcessors.size(); i++) {
                InputProcessor p = inputProcessors.get(i);
                p.scrolled(amount);
            }
        }
        return true;
    }

    @NotNull
    public ArrayList<InputProcessor> getInputProcessors() {
        return inputProcessors;
    }

}
