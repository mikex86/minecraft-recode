package me.gommeantilegit.minecraft.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.gommeantilegit.minecraft.texture.rendering.ImageView;
import org.jetbrains.annotations.NotNull;

public class TextureWrapper {

    @NotNull
    protected final Texture glTexture;

    @NotNull
    private final ImageView imageView;

    public TextureWrapper(@NotNull String textureResource, @NotNull SpriteBatch spriteBatch) {
        this.glTexture = new Texture(Gdx.files.classpath(textureResource));
        this.imageView = new ImageView(glTexture, spriteBatch);
    }

    public void drawUVRect(float x, float y, float width, float height, int[] pixelPositions, int[] pixelSize) {
        this.imageView.drawUVTexturedRect(x, y, width, height,
                (float) pixelPositions[0] / glTexture.getWidth(), (float) pixelPositions[1] / glTexture.getHeight(),
                (float) pixelSize[0] / glTexture.getWidth(), (float) pixelSize[1] / glTexture.getHeight(), 1, 1, 1, 1);
    }
}
