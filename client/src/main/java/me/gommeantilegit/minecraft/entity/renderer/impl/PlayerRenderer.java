package me.gommeantilegit.minecraft.entity.renderer.impl;

import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.RenderablePlayer;
import me.gommeantilegit.minecraft.entity.renderer.IEntityRenderer;
import me.gommeantilegit.minecraft.shader.api.CommonShader;
import org.jetbrains.annotations.NotNull;

/**
 * Rendering object to render players
 */
public class PlayerRenderer implements IEntityRenderer<CommonShader, RenderablePlayer> {

    @Override
    public void renderEntity(@NotNull RenderablePlayer entity, float partialTicks, @NotNull CommonShader shader) {
        if (!entity.isVisible()) return;

        float rotationYaw = entity.lastRotationYaw + (entity.rotationYawTicked - entity.lastRotationYaw) * partialTicks;

        Entity.EntityRenderPosition playerRenderPosition = entity.getEntityRenderPosition();

        float realX = playerRenderPosition.lastPosX + (playerRenderPosition.posX - playerRenderPosition.lastPosX) * partialTicks;
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

        entity.getModel().render(partialTicks, entity, shader);
        shader.popMatrix();
    }

}
