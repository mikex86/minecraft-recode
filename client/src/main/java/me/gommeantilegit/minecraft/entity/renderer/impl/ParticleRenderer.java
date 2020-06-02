package me.gommeantilegit.minecraft.entity.renderer.impl;

import com.badlogic.gdx.graphics.Camera;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.entity.renderer.IEntityRenderer;
import me.gommeantilegit.minecraft.entity.particle.Particle;
import me.gommeantilegit.minecraft.shader.api.CommonShader;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import org.jetbrains.annotations.NotNull;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;

public class ParticleRenderer implements IEntityRenderer<CommonShader, Particle> {

    @Override
    public void renderEntity(@NotNull Particle particle, float partialTicks, @NotNull CommonShader shader) {
        if (shader instanceof StdShader)
            ((StdShader) shader).disableLighting();
        assert particle.getMesh() != null;
        float posX = particle.lastPosX + (particle.posX - particle.lastPosX) * partialTicks;
        float posY = particle.lastPosY + (particle.posY - particle.lastPosY) * partialTicks;
        float posZ = particle.lastPosZ + (particle.posZ - particle.lastPosZ) * partialTicks;

        Camera camera = ((ClientMinecraft) particle.world.mc).thePlayer.camera;

        double yaw, pitch;
        {
            pitch = -toDegrees(asin(camera.direction.y));
            yaw = toDegrees(atan2(camera.direction.x, camera.direction.z));
        }

        {
            shader.pushMatrix();
            shader.translate(posX, posY, posZ);
            shader.rotate(0, 1, 0, (float) yaw);
            shader.rotate(1, 0, 0, (float) pitch);
            particle.getModel().render(partialTicks, particle, shader);
            shader.popMatrix();
        }
        if (shader instanceof StdShader)
            ((StdShader) shader).enableLighting();
    }
}
