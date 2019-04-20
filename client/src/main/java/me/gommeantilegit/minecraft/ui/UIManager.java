package me.gommeantilegit.minecraft.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.font.FontRenderer;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.ui.render.Overlay2D;
import me.gommeantilegit.minecraft.ui.screen.GuiScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UIManager extends Overlay2D implements Tickable {

    /**
     * Singleton Font Renderer instance
     */
    @NotNull
    public final FontRenderer fontRenderer;

    /**
     * Parent minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * The current GuiScreen instance to be rendered
     */
    @Nullable
    public GuiScreen currentScreen;

    /**
     * @param spriteBatch sets {@link #spriteBatch}
     */
    public UIManager(@NotNull SpriteBatch spriteBatch, @NotNull ClientMinecraft mc) {
        super(spriteBatch);
        this.mc = mc;
        this.fontRenderer = new FontRenderer(this.spriteBatch);
    }

    /**
     * Makes the specified GuiScreen instance the current screen that is being rendered
     *
     * @param guiScreen the new GuiScreen instance to be displayed
     */
    public void displayGuiScreen(@Nullable GuiScreen guiScreen) {
        if (currentScreen != null) {
            this.mc.inputHandler.unregisterProcessor(currentScreen);
            currentScreen.onGuiClosed(guiScreen);
        }
        this.currentScreen = guiScreen;
        if (currentScreen != null) {
            this.currentScreen.setSpriteBatch(this.spriteBatch);
            this.currentScreen.setMinecraft(mc);

            synchronized (currentScreen.buttons) {
                this.currentScreen.initGui(DPI.scaledWidthi, DPI.scaledHeighti);
            }
            this.mc.inputHandler.registerInputProcessor(guiScreen);
        }
    }

    @Override
    public void render() {
        if (currentScreen != null)
            currentScreen.render();
    }

    /**
     * Re-sizes the current gui screen (if present) to the new scaled width / height.
     *
     * @param scaledWidth  the new scaled width
     * @param scaledHeight the new scaled height
     * @see me.gommeantilegit.minecraft.hud.scaling.DPI for more info
     */
    public void resize(int scaledWidth, int scaledHeight) {
        if (currentScreen != null)
            currentScreen.resize(scaledWidth, scaledHeight);
    }

    /**
     * Updates the screen tick based, if one is currently displayed
     */
    @Override
    public void tick(float partialTicks) {
        if (currentScreen != null)
            currentScreen.tick(partialTicks);
    }
}
