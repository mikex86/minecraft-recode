package me.gommeantilegit.minecraft.ui.button;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.font.FontRenderer;
import me.gommeantilegit.minecraft.sound.SoundResource;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiButton extends TexturedButton implements Tickable {

    /**
     * Parent Minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * The text that the button displays
     */
    @NotNull
    private String displayText;

    /**
     * String width of {@link #displayText}
     */
    private int stringWidth = -1;

    /**
     * @param displayText the text that the button displays
     * @param width       sets {@link #width}
     * @param height      sets {@link #height}
     * @param posX        sets {@link #posX}
     * @param posY        sets {@link #posY}
     * @param mc          client minecraft instance
     */
    public GuiButton(@NotNull String displayText, int width, int height, int posX, int posY, ClientMinecraft mc) {
        super(width, height, posX, posY, mc.textureManager.guiTextures, null);
        this.displayText = displayText;
        this.mc = mc;
    }

    /**
     * @param displayText the text that the button displays
     * @param posX        sets {@link #posX}
     * @param posY        sets {@link #posY}
     * @param mc          client minecraft instance
     */
    public GuiButton(@NotNull String displayText, int posX, int posY, ClientMinecraft mc) {
        this(displayText, 200, 20, posX, posY, mc);
    }

    @Override
    public void render(float mouseX, float mouseY) {
        if (stringWidth == -1)
            stringWidth = mc.uiManager.fontRenderer.getStringWidth(displayText);

        super.render(mouseX, mouseY);
        int color; // Button text color
        if (disabled) {
            color = 0xffa0a0a0;
        } else if (hovered) {
            color = 0xffffa0;
        } else {
            color = 0xe0e0e0;
        }
        FontRenderer fontRenderer = mc.uiManager.fontRenderer;
        //noinspection IntegerDivisionInFloatingPointContext
        fontRenderer.drawStringWithShadow(this.displayText, posX + width / 2f - (this.stringWidth / 2), posY + height / 2f - fontRenderer.fontHeight / 2f, color);
    }

    @Override
    public void draw(boolean hovered) {
        this.hovered = hovered;
        int buttonState = disabled ? 0 : hovered ? 2 : 1;
        this.textureWrapper.imageView.spriteBatch.begin();
        this.textureWrapper.drawUVRect(posX, posY, width / 2f, height, 0, 46 + buttonState * 20, width / 2, height, 1, 1, 1, 1, false);
        this.textureWrapper.drawUVRect(posX + width / 2f, posY, width / 2f, height, 200 - width / 2, 46 + buttonState * 20, width / 2, height, 1, 1, 1, 1, false);
        this.textureWrapper.imageView.spriteBatch.end();
    }

    /**
     * Called to update the button on minecraft tick.
     * CAUTION: NO OPENGL CONTEXT
     * @param partialTicks the ticks to be performed in the current frame being rendered.
     */
    @Override
    public void tick(float partialTicks) {

    }

    @Override
    protected void onMouseDown() {
        SoundResource.getSound("sound/random/click").play(0.5f, 1);
    }

    @NotNull
    @Override
    public GuiButton setOnClickListener(@NotNull OnClickListener onClickListener) {
        return (GuiButton) super.setOnClickListener(onClickListener);
    }

    @NotNull
    @Override
    public GuiButton setOnPressedListener(@Nullable OnPressedListener onPressedListener) {
        return (GuiButton) super.setOnPressedListener(onPressedListener);
    }

    @NotNull
    @Override
    public GuiButton setOnMouseDownListener(@Nullable OnMouseDownListener onMouseDownListener) {
        return (GuiButton) super.setOnMouseDownListener(onMouseDownListener);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public GuiButton setDisplayText(@NotNull String displayText) {
        this.displayText = displayText;
        this.stringWidth = mc.uiManager.fontRenderer.getStringWidth(displayText);
        return this;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
