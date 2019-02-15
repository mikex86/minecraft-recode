package me.gommeantilegit.minecraft.ui.render;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.jetbrains.annotations.NotNull;

public abstract class Overlay2D extends InputAdapter {

    /**
     * The sprite-batch instance used for drawing
     */
    @NotNull
    protected SpriteBatch spriteBatch;

    /**
     * @param spriteBatch sets {@link #spriteBatch}
     */
    protected Overlay2D(@NotNull SpriteBatch spriteBatch) {
        this.spriteBatch = spriteBatch;
    }

    /**
     * Called to render the overlay
     */
    public abstract void render();

    /**
     * @param spriteBatch sets {@link #spriteBatch}
     */
    public void setSpriteBatch(@NotNull SpriteBatch spriteBatch) {
        this.spriteBatch = spriteBatch;
    }

}
