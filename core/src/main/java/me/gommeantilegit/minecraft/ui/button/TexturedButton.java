package me.gommeantilegit.minecraft.ui.button;

import org.jetbrains.annotations.NotNull;

import me.gommeantilegit.minecraft.texture.TextureWrapper;

public class TexturedButton extends Button {

    /**
     * The texture wrapper handling the drawing for the texture which should be used for rendering of the button
     */
    @NotNull
    private TextureWrapper textureWrapper;

    /**
     * Normalized alpha value
     */
    public double transparency;

    /**
     * The texture region where the button texture is inside the texture handled by the wrapper {@link #textureWrapper}
     */
    @NotNull
    private TextureWrapper.RenderData renderData;

    /**
     * @param width          sets {@link #width}
     * @param height         sets {@link #height}
     * @param posX           sets {@link #posX}
     * @param posY           sets {@link #posY}
     * @param textureWrapper sets {@link #textureWrapper}
     * @param renderData     sets {@link #renderData}
     */
    public TexturedButton(int width, int height, int posX, int posY, @NotNull TextureWrapper textureWrapper, @NotNull TextureWrapper.RenderData renderData) {
        super(width, height, posX, posY);
        this.textureWrapper = textureWrapper;
        this.renderData = renderData;
    }

    /**
     * @param hovered state if the button is draggedInside by cursor
     */
    @Override
    public void draw(boolean hovered) {
        textureWrapper.drawUVRect(posX, posY, width, height, renderData, 1, 1, 1, (float) this.transparency);
    }
}
