package me.gommeantilegit.minecraft.texture.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import me.gommeantilegit.minecraft.ClientMinecraft;
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
     * Block destroy stages textures
     */
    @NotNull
    public final CustomTexture[] blockDestroyStages;

    /**
     * The clouds texture
     */
    @NotNull
    public final CustomTexture cloudsTexture;

    /**
     * @param mc          the parent minecraft instance
     * @param spriteBatch 2D sprite batch
     */
    public TextureManager(@NotNull ClientMinecraft mc, @NotNull SpriteBatch spriteBatch) {
        guiTextures = new GuiTextures(spriteBatch);
        hudTextures = new HudTextures(spriteBatch);
        blockDestroyStages = new CustomTexture[]{
                new CustomTexture(Gdx.files.classpath("textures/blocks/destroy/destroy_stage_0.png")),
                new CustomTexture(Gdx.files.classpath("textures/blocks/destroy/destroy_stage_1.png")),
                new CustomTexture(Gdx.files.classpath("textures/blocks/destroy/destroy_stage_2.png")),
                new CustomTexture(Gdx.files.classpath("textures/blocks/destroy/destroy_stage_3.png")),
                new CustomTexture(Gdx.files.classpath("textures/blocks/destroy/destroy_stage_4.png")),
                new CustomTexture(Gdx.files.classpath("textures/blocks/destroy/destroy_stage_5.png")),
                new CustomTexture(Gdx.files.classpath("textures/blocks/destroy/destroy_stage_6.png")),
                new CustomTexture(Gdx.files.classpath("textures/blocks/destroy/destroy_stage_7.png")),
                new CustomTexture(Gdx.files.classpath("textures/blocks/destroy/destroy_stage_8.png")),
                new CustomTexture(Gdx.files.classpath("textures/blocks/destroy/destroy_stage_9.png"))
        };
        blockTextureMap = new BlockTextureMap(mc, mc.blockRendererRegistry);
        cloudsTexture = new CustomTexture(Gdx.files.classpath("textures/environment/clouds.png"));
        cloudsTexture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
    }
}
