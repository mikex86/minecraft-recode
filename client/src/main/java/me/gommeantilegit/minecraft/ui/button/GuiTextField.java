package me.gommeantilegit.minecraft.ui.button;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import me.gommeantilegit.minecraft.ClientMinecraft;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a TextField where the user can type in text
 */
public class GuiTextField extends GuiButton {

    /**
     * Current user input string buffer
     */
    @NotNull
    protected final StringBuilder stringBuilder;

    /**
     * Parent client minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * Boolean state whether the text field is currently selected
     */
    private boolean selected;

    /**
     * Amount of ticks the text field has been selected modulo 13 for blinking if text field is selected
     */
    private int ticksSelected = 0;

    /**
     * Max length of the text field.
     * -1 if none
     */
    private int maxLength = -1;

    public GuiTextField(@NotNull String initialString, int width, int height, int posX, int posY, @NotNull ClientMinecraft mc) {
        super("", width, height, posX, posY, mc);
        this.mc = mc;
        this.stringBuilder = new StringBuilder(initialString);
    }

    @Override
    public void draw(boolean hovered) {

        mc.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        mc.shapeRenderer.setColor(0.627451f, 0.627451f, 0.627451f, 1);
        mc.shapeRenderer.rect(posX - 1, posY - 1, width + 2, height + 2);
        mc.shapeRenderer.setColor(0, 0, 0, 1);
        mc.shapeRenderer.rect(posX, posY, width, height);
        mc.shapeRenderer.end();

        boolean showUnderscore = selected && (ticksSelected / 6) % 2 == 0;
        mc.uiManager.fontRenderer.drawStringWithShadow(this.stringBuilder.toString() + (showUnderscore ? "_" : ""), posX + 4, posY + height / 2f - mc.uiManager.fontRenderer.fontHeight / 2f, disabled ? 0x707070 : 0xe0e0e0);

    }

    @Override
    public boolean keyTyped(char character) {
        if (selected) {
            if (character == '\b') {
                if (stringBuilder.length() > 0) {
                    stringBuilder.setLength(stringBuilder.length() - 1);
                }
            } else if (maxLength == -1 || stringBuilder.length() < maxLength && character >= 32)
                stringBuilder.append(character);
        }
        return super.keyTyped(character);
    }

    @Override
    protected void onMouseDown() {
        selected = true;
    }

    @Override
    protected void onMouseClickedOutside() {
        selected = false;
    }

    @Override
    public void tick(float partialTicks) {
        if (selected)
            ticksSelected = (ticksSelected + 1) % 13;
    }

    public GuiTextField setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * @return the string the the user has typed in
     */
    @NotNull
    public String getTypedText() {
        return this.stringBuilder.toString();
    }

}