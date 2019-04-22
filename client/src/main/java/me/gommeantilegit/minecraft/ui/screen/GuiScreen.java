package me.gommeantilegit.minecraft.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.BooleanSetting;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.PercentSetting;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.Setting;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.StringSelectionSetting;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.texture.TextureWrapper;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.ui.button.BooleanValueSwitchButton;
import me.gommeantilegit.minecraft.ui.button.GuiButton;
import me.gommeantilegit.minecraft.ui.button.Slider;
import me.gommeantilegit.minecraft.ui.button.StringValueSwitchButton;
import me.gommeantilegit.minecraft.ui.render.Overlay2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiScreen extends Overlay2D implements Tickable {

    /**
     * A list of all gui buttons of the screen
     */
    @NotNull
    public final List<GuiButton> buttons = new ArrayList<>();

    /**
     * Parent Minecraft instance. Null until {@link #initGui(int, int)} is called!
     */
    protected ClientMinecraft mc;

    /**
     * The texture wrapper instance for the default background texture (dirt)
     */
    private static TextureWrapper backgroundTextureWrapper;

    protected GuiScreen() {
        super(null);
    }

    /**
     * Draws a screen full of dirt textures
     */
    protected void drawDefaultBackground() {
        if (backgroundTextureWrapper == null) {
            backgroundTextureWrapper = new TextureWrapper("textures/gui/gui_background.png", spriteBatch);
            backgroundTextureWrapper.getGlTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        }
        float size = 32;
        int x = 0, y = 0, width = DPI.scaledWidthi, height = DPI.scaledHeighti;
        backgroundTextureWrapper.imageView.drawUVTexturedRect(x, y, width, height,
                0, 0, width / size, height / size, 0.2509804f, 0.2509804f, 0.2509804f, 1.0f, true);
    }

    /**
     * Super must be called to draw buttons after the inherit finishes drawing
     */
    @Override
    public void render() {
        int mouseX = DPI.getScaledPixelPos(Gdx.input.getX(), 0), mouseY = DPI.getScaledPixelPos(Gdx.input.getY(), 1);
        for (GuiButton button : buttons) {
            button.render(mouseX, mouseY);
        }
    }


    /**
     * Adds option buttons that allow the user to change the game-settings SOUND_RESOURCES
     *
     * @param scaledWidth  the scaled width
     * @param scaledHeight the scaled height
     */
    protected void addOptions(int scaledWidth, int scaledHeight, Setting[] settings) {
        int i = 0;
        for (Setting generalSetting : settings) {
            if (generalSetting instanceof StringSelectionSetting) {
                this.buttons.add(new StringValueSwitchButton((StringSelectionSetting) generalSetting, (scaledWidth / 2 - 155) + (i % 2) * 160, scaledHeight / 6 + 24 * (i >> 1), mc));
            } else if (generalSetting instanceof BooleanSetting) {
                this.buttons.add(new BooleanValueSwitchButton((BooleanSetting) generalSetting, (scaledWidth / 2 - 155) + (i % 2) * 160, scaledHeight / 6 + 24 * (i >> 1), mc) {
                    @NotNull
                    @Override
                    protected String getDisplayText(BooleanSetting selectionSetting) {
                        return selectionSetting.getName() + ": " + (selectionSetting.getValue() ? "ON" : "OFF");
                    }
                });
            } else if (generalSetting instanceof PercentSetting) {
                this.buttons.add(new Slider((PercentSetting) generalSetting, (scaledWidth / 2 - 155) + (i % 2) * 160, scaledHeight / 6 + 24 * (i >> 1), mc));
            }
            i++;
        }
    }

    @Override
    public void tick(float partialTicks) {
        synchronized (buttons) {
            for (GuiButton button : buttons) {
                button.tick(partialTicks);
            }
        }
    }

    /**
     * Called when the Gui is closed
     * @param newScreen the new screen that is displayed next. Null if none is displayed.
     */
    public void onGuiClosed(@Nullable GuiScreen newScreen){
    }

    @Override
    public boolean keyTyped(char character) {
        for (GuiButton button : buttons) {
            button.keyTyped(character);
        }
        return super.keyTyped(character);
    }

    @Override
    public boolean keyDown(int keycode) {
        for (GuiButton button : buttons) {
            button.keyDown(keycode);
        }
        return super.keyDown(keycode);
    }

    /**
     * Invokes listeners of all buttons. super.touchDown(..) must thus always be called
     *
     * @param screenX x pos
     * @param screenY y pos
     * @param pointer cursor / touch pointer
     * @param button  button it
     * @return state
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (GuiButton guiButton : buttons) {
            guiButton.touchDown(screenX, screenY, pointer, button);
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    /**
     * Invokes listeners of all buttons. super.touchDown(..) must thus always be called
     *
     * @param screenX x pos
     * @param screenY y pos
     * @param pointer cursor / touch pointer
     * @param button  button it
     * @return state
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (GuiButton guiButton : buttons) {
            guiButton.touchUp(screenX, screenY, pointer, button);
        }
        return super.touchUp(screenX, screenY, pointer, button);
    }

    /**
     * Called to initialize the GuiScreen when it is displayed or when resolution has changed.
     * super.initGui(width, height) must be called if this method is overridden.
     *
     * @param scaledWidth  the new scaled width
     * @param scaledHeight the new scaled height
     * @see DPI for more information about scaled variables
     */
    public void initGui(int scaledWidth, int scaledHeight) {
        this.buttons.clear();
    }

    /**
     * Called to resize the GuiScreen. GuiScreen will adapt to the new resolution.
     *
     * @param scaledWidth  the new scaled width value
     * @param scaledHeight the new scaled height value
     * @see DPI for more information about scaled variables
     */
    public void resize(int scaledWidth, int scaledHeight) {
        synchronized (buttons) {
            this.initGui(scaledWidth, scaledHeight);
        }
    }

    /**
     * Sets the Minecraft instance of the GuiScreen to the specified instance
     *
     * @param mc the minecraft instance
     */
    public void setMinecraft(@NotNull ClientMinecraft mc) {
        this.mc = mc;
    }

    private final Color temp1 = new Color(), temp2 = new Color();

    /**
     * Draws a gradient rect in region [x, y, x + width, y + height]
     *
     * @param x      x start coord.
     * @param y      y start coord.
     * @param width  rectangle width
     * @param height rectangle height
     * @param color1 argb color code 1
     * @param color2 argb color code 2
     */
    protected void drawGradientRect(int x, int y, float width, float height, int color1, int color2) {
        float r1 = (float) (color1 >> 16 & 255) / 255.0F,
                g1 = (float) (color1 & 255) / 255.0F,
                b1 = (float) (color1 >> 8 & 255) / 255.0F,
                a1 = (float) (color1 >> 24 & 255) / 255.0F;
        float r2 = (float) (color2 >> 16 & 255) / 255.0F,
                g2 = (float) (color2 & 255) / 255.0F,
                b2 = (float) (color2 >> 8 & 255) / 255.0F,
                a2 = (float) (color2 >> 24 & 255) / 255.0F;
        temp1.set(r1, g1, b1, a1);
        temp2.set(r2, g2, b2, a2);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        mc.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        mc.shapeRenderer.rect(x, y, width, height, temp1, temp1, temp2, temp2);
        mc.shapeRenderer.end();
    }
}
