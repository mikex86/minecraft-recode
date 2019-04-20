package me.gommeantilegit.minecraft.ui.button;

import com.badlogic.gdx.Gdx;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.ui.render.Renderable;

public abstract class Button extends Renderable {

    /**
     * Width and height of the button
     */
    protected int width, height;

    /**
     * Position of the button
     */
    protected int posX, posY;

    /**
     * State if the button is currently being pressed
     */
    protected boolean pressed;

    /**
     * State if the button is disabled.
     * This means the button cannot be clicked an is displayed as grayed out.
     */
    protected boolean disabled;

    /**
     * Represents a listener invoked on mouse click when the button is draggedInside
     */
    public interface OnClickListener {
        /**
         * Called on button click
         *
         * @param pointer touch pointer
         */
        void onClick(int pointer);
    }

    public interface OnPressedListener {

        /**
         * Called every frame if the button is held down
         *
         * @param pointer touch pointer
         */
        void onHeldDown(int pointer);
    }

    public interface OnDragEnterListener {

        /**
         * Called if a pointer enters the field of the button and is pressed. Also invoked on button down
         *
         * @param pointer touch pointer - the one that entered the region
         */
        void onEntered(int pointer);
    }

    public interface OnDragLeaveListener {

        /**
         * Called if a pointer leaves the field of the button and is pressed or if the pointer released the drag.
         *
         * @param pointer touch pointer - the one that left the region
         */
        void onLeave(int pointer);
    }

    public interface OnMouseDownListener {

        /**
         * Invoked on button touch down
         *
         * @param pointer touch pointer
         */
        void onMouseDown(int pointer);
    }

    public interface OnMouseReleasedListener {

        /**
         * Invoked on button touch up
         *
         * @param pointer touch pointer
         */
        void onMouseRelease(int pointer);
    }

    /**
     * The Listener invoked on button click (mouse release)
     */
    @Nullable
    protected OnClickListener onClickListener;

    /**
     * The Listener invoked on button down
     */
    @Nullable
    protected OnPressedListener onPressedListener;

    @Nullable
    protected OnMouseDownListener onMouseDownListener;

    @Nullable
    protected OnMouseReleasedListener onMouseReleasedListener;

    @Nullable
    protected OnDragEnterListener onDragEnterListener;

    @Nullable
    protected OnDragLeaveListener onDragLeaveListener;


    /**
     * The "finger" index the button was clicked with
     */
    protected int clickPointer = -1;

    /**
     * State if the button is dragged by a pointer in the it's field
     */
    protected boolean draggedInside;

    /**
     * The "finger" index that started dragging inside the field of the button
     */
    protected int dragInsidePointer = -1;

    public Button(int width, int height, int posX, int posY) {
        this.width = width;
        this.height = height;
        this.posX = posX;
        this.posY = posY;
    }

    /**
     * Sets {@link #onClickListener}
     *
     * @param onClickListener the new value of {@link #onClickListener}
     * @return accessed button instance
     */
    @NotNull
    public Button setOnClickListener(@NotNull Button.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    /**
     * Sets {@link #onPressedListener}
     *
     * @param onPressedListener the new value of {@link #onPressedListener}
     * @return accessed button instance
     */
    @NotNull
    public Button setOnHeldListener(@NotNull OnPressedListener onPressedListener) {
        this.onPressedListener = onPressedListener;
        return this;
    }

    /**
     * @param mouseX cursor x pos
     * @param mouseY cursor y pos
     */
    public void render(float mouseX, float mouseY) {
        if (pressed)
            if (onPressedListener != null && !disabled)
                onPressedListener.onHeldDown(this.clickPointer);
        draw(isHovered(mouseX, mouseY));
    }

    /**
     * Processes the input of {@link #touchDown(int, int, int, int)} and {@link #touchUp(int, int, int, int)}
     *
     * @param mouseX   cursor x position (scaled)
     * @param mouseY   cursor y position (scaled)
     * @param buttonID the id of the button. Can be 0, 1, 2. 0 -> left click; 1 -> right click; 2 -> mid click;
     * @param action   the action performed. 0 -> press; 1 -> release;
     * @param pointer  the pointer that performed the input
     */
    public void receiveMouseInput(float mouseX, float mouseY,
                                  @MagicConstant(intValues = {0, 1, 2}) int buttonID,
                                  @MagicConstant(intValues = {0, 1}) int action, int pointer) {
        switch (action) {
            case 0:
                if (buttonID == 0)
                    if (isHovered(mouseX, mouseY) && this.clickPointer == -1 && !disabled) {
                        onMouseDown();
                        if (this.onMouseDownListener != null)
                            this.onMouseDownListener.onMouseDown(pointer);
                        pressed = true;
                        this.clickPointer = pointer;
                    }
                break;
            case 1:
                if (buttonID == 0) {
                    if (pressed && clickPointer == pointer && !disabled) {
                        onClick();
                        if (onClickListener != null) {
                            onClickListener.onClick(pointer);
                        }
                        if (this.onMouseReleasedListener != null)
                            this.onMouseReleasedListener.onMouseRelease(pointer);
                        pressed = false;
                        clickPointer = -1;
                    } else {
                        onMouseClickedOutside();
                    }
                }
                break;
        }

    }

    /**
     * Called on click when the button is not hovered
     */
    protected void onMouseClickedOutside() {
    }

    /**
     * Called on click
     */
    protected void onClick() {
    }

    /**
     * Called on mouse down
     */
    protected void onMouseDown() {
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        int mouseX = DPI.getScaledPixelPos(Gdx.input.getX(), 0), mouseY = DPI.getScaledPixelPos(Gdx.input.getY(), 1); // Scaled cursor position
        receiveMouseInput(mouseX, mouseY, button, 0, pointer);
        if (!this.draggedInside && this.dragInsidePointer == -1 && button == 0 && isHovered(mouseX, mouseY) && !disabled) {
            onDragEnter(pointer);
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        int mouseX = DPI.getScaledPixelPos(Gdx.input.getX(), 0), mouseY = DPI.getScaledPixelPos(Gdx.input.getY(), 1); // Scaled cursor position
        receiveMouseInput(mouseX, mouseY, button, 1, pointer);
        if (this.draggedInside && this.dragInsidePointer == pointer && button == 0 && !disabled) {
            onDragExit(pointer);
        }
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        int mouseX = DPI.getScaledPixelPos(Gdx.input.getX(), 0), mouseY = DPI.getScaledPixelPos(Gdx.input.getY(), 1); // Scaled cursor position
        if (this.dragInsidePointer == -1 && !draggedInside && isHovered(mouseX, mouseY) && !disabled) {
            onDragEnter(pointer);
        }
        if (dragInsidePointer != -1 && draggedInside && pointer == this.dragInsidePointer && !isHovered(mouseX, mouseY)) {
            onDragExit(pointer);
        }
        return super.touchDragged(screenX, screenY, pointer);
    }

    /**
     * Function handling the {@link OnDragLeaveListener}
     *
     * @param pointer touch pointer
     */
    private void onDragExit(int pointer) {
        this.dragInsidePointer = -1;
        this.draggedInside = false;
        if (this.onDragLeaveListener != null && !disabled)
            this.onDragLeaveListener.onLeave(pointer);
    }

    /**
     * Function handling the {@link OnDragEnterListener}
     *
     * @param pointer touch pointer
     */
    private void onDragEnter(int pointer) {
        this.dragInsidePointer = pointer;
        this.draggedInside = true;
        if (this.onDragEnterListener != null && !disabled)
            this.onDragEnterListener.onEntered(pointer);
    }

    /**
     * Draws the button
     *
     * @param hovered state if the button is draggedInside by cursor
     */
    protected abstract void draw(boolean hovered);

    /**
     * @param mouseX cursor x pos
     * @param mouseY cursor y pos
     * @return true if the button is draggedInside by the cursor with the given x and y position.
     */
    public boolean isHovered(float mouseX, float mouseY) {
        return (mouseX >= posX &&
                mouseX <= posX + width) &&
                (mouseY >= posY && mouseY <= posY + height);
    }

    @NotNull
    public Button setOnPressedListener(@Nullable OnPressedListener onPressedListener) {
        this.onPressedListener = onPressedListener;
        return this;
    }

    @NotNull
    public Button setOnMouseDownListener(@Nullable OnMouseDownListener onMouseDownListener) {
        this.onMouseDownListener = onMouseDownListener;
        return this;
    }

    public void setOnMouseReleasedListener(@Nullable OnMouseReleasedListener onMouseReleasedListener) {
        this.onMouseReleasedListener = onMouseReleasedListener;
    }

    public void setOnDragLeaveListener(@Nullable OnDragLeaveListener onDragLeaveListener) {
        this.onDragLeaveListener = onDragLeaveListener;
    }

    public void setOnDragEnterListener(@Nullable OnDragEnterListener onDragEnterListener) {
        this.onDragEnterListener = onDragEnterListener;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }
}
