package me.gommeantilegit.minecraft.entity.renderer.impl;

import com.badlogic.gdx.Gdx;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.entity.renderer.model.impl.PlayerModel;
import me.gommeantilegit.minecraft.entity.renderer.IEntityRenderer;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import org.jetbrains.annotations.NotNull;

/**
 * Rendering object to render players
 */
public class PlayerRenderer implements IEntityRenderer<PlayerBase, StdShader> {

    /**
     * Texture used by the {@link #PLAYER_MODEL}
     */
    @NotNull
    private static final CustomTexture PLAYER_TEXTURE = new CustomTexture(Gdx.files.classpath("textures/entities/steve.png"));

    /**
     * Player model for rendering
     */
    @NotNull
    private static final PlayerModel PLAYER_MODEL = new PlayerModel(PLAYER_TEXTURE);

    @Override
    public void renderEntity(@NotNull PlayerBase entity, float partialTicks, @NotNull StdShader shader) {
        if (!entity.isVisible()) return;

        float rotationYaw = entity.lastRotationYaw + (entity.rotationYawTicked - entity.lastRotationYaw) * partialTicks;

        Entity.EntityRenderPosition playerRenderPosition = entity.getEntityRenderPosition();
        float realX = entity.getEntityRenderPosition().lastPosX + (playerRenderPosition.posX - playerRenderPosition.lastPosX) * partialTicks;
        float realY = playerRenderPosition.lastPosY + (playerRenderPosition.posY - playerRenderPosition.lastPosY) * partialTicks;
        float realZ = playerRenderPosition.lastPosZ + (playerRenderPosition.posZ - playerRenderPosition.lastPosZ) * partialTicks;

        shader.pushMatrix();

        shader.translate(
                realX,
                realY,
                realZ
        );

        shader.scale(-1, -1, -1);

        shader.rotate(0.0f, 1.0f, 0.0f, rotationYaw);

        PLAYER_MODEL.render(partialTicks, entity, shader);
        shader.popMatrix();
    }

}
