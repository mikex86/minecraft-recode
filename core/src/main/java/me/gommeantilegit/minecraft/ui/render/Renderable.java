package me.gommeantilegit.minecraft.ui.render;

import com.badlogic.gdx.InputAdapter;

public abstract class Renderable extends InputAdapter {

    /**
     * Called to render the object
     *
     * @param mouseX cursor x position
     * @param mouseY cursor y position
     */
    public abstract void render(float mouseX, float mouseY);

}
