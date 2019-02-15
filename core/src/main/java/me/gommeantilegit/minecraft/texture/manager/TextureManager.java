package me.gommeantilegit.minecraft.texture.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.texture.texturewrappers.HudTextures;
import org.jetbrains.annotations.NotNull;

import me.gommeantilegit.minecraft.block.texturemap.BlockTextureMap;
import me.gommeantilegit.minecraft.texture.texturewrappers.GuiTextures;

public class TextureManager {

    /**
     * GuITextures instance
     */
    @NotNull
    public final GuiTextures guiTextures;

    /**
     * Hud textures instance
     */
    @NotNull
    public final HudTextures hudTextures;

    /**
     * TextureMap object
     */
    @NotNull
    public final BlockTextureMap blockTextureMap;

    /**
     * Block destroy stages texture
     */
    @NotNull
    public final CustomTexture blockDestroyStages;

    /**
     * @param spriteBatch 2D sprite batch
     */
    public TextureManager(@NotNull SpriteBatch spriteBatch) {
        guiTextures = new GuiTextures(spriteBatch);
        blockTextureMap = new BlockTextureMap(256);
        hudTextures = new HudTextures(spriteBatch);
        blockDestroyStages = new CustomTexture(Gdx.files.classpath("textures/blocks/destroy/destory_stages.png"));
    }
}
