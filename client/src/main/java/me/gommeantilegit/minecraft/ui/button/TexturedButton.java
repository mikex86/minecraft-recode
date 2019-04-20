package me.gommeantilegit.minecraft.ui.button;

import me.gommeantilegit.minecraft.texture.TextureWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TexturedButton extends Button {

    /**
     * The texture wrapper handling the drawing for the texture which should be used for rendering of the button
     */
    @NotNull
    protected TextureWrapper textureWrapper;

    /**
     * Normalized alpha value
     */
    public double transparency = 1f;

    /**
     * State whether the button is hovered by the cursor
     */
    protected boolean hovered;

    /**
     * The texture region where the button texture is inside the texture handled by the wrapper {@link #textureWrapper}
     */
    @Nullable
    protected final TextureWrapper.RenderData renderData;

    /**
     * @param width          sets {@link #width}
     * @param height         sets {@link #height}
     * @param posX           sets {@link #posX}
     * @param posY           sets {@link #posY}
     * @param textureWrapper sets {@link #textureWrapper}
     * @param renderData     sets {@link #renderData}
     */
    public TexturedButton(int width, int height, int posX, int posY, @NotNull TextureWrapper textureWrapper, @Nullable TextureWrapper.RenderData renderData) {
        super(width, height, posX, posY);
        this.textureWrapper = textureWrapper;
        this.renderData = renderData;
    }

    /**
     * @param hovered state if the button is draggedInside by cursor
     */
    @Override
    public void draw(boolean hovered) {
        this.hovered = hovered;
        TextureWrapper.RenderData renderData = getRenderData(hovered);
        if (renderData != null)
            textureWrapper.drawUVRect(posX, posY, width, height, renderData, 1, 1, 1, (float) this.transparency, true);
    }

    @Nullable
    protected TextureWrapper.RenderData getRenderData(boolean hovered) {
        return renderData;
    }

    public boolean isHovered() {
        return hovered;
    }
}
