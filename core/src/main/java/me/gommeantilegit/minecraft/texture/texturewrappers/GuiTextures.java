package me.gommeantilegit.minecraft.texture.texturewrappers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.jetbrains.annotations.NotNull;

import me.gommeantilegit.minecraft.texture.TextureWrapper;

/**
 * Texture wrapper for "textures/gui/gui.png"
 */
public class GuiTextures extends TextureWrapper {

    public GuiTextures(@NotNull SpriteBatch spriteBatch) {
        super("textures/gui/gui.png", spriteBatch);
    }

}
