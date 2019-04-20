package me.gommeantilegit.minecraft.texture.rendering;

import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.jetbrains.annotations.NotNull;

public class ImageView {

    @NotNull
    private final CustomTexture glTexture;

    @NotNull
    public final SpriteBatch spriteBatch;

    public ImageView(@NotNull CustomTexture glTexture, @NotNull SpriteBatch spriteBatch) {
        this.glTexture = glTexture;
        this.spriteBatch = spriteBatch;
    }

    public void drawUVTexturedRect(float x, float y, float width, float height, float u, float v, float textureWidth, float textureHeight, float r, float g, float b, float a, boolean flush) {
        if (flush)
            this.spriteBatch.begin();
        this.spriteBatch.setColor(r, g, b, a);
        this.spriteBatch.draw(glTexture, x, y, width, height, u, v, u + textureWidth, v + textureHeight);
        this.spriteBatch.setColor(1, 1, 1, 1);
        if (flush)
            this.spriteBatch.end();
    }
}
