package me.gommeantilegit.minecraft.entity.renderer.impl;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.entity.renderer.IEntityRenderer;
import me.gommeantilegit.minecraft.entity.particle.Particle;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.util.AngleUtils;
import org.jetbrains.annotations.NotNull;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;

public class ParticleRenderer implements IEntityRenderer<Particle, StdShader> {

    @Override
    public void renderEntity(@NotNull Particle particle, float partialTicks, @NotNull StdShader shader) {
        shader.disableLighting();
        assert particle.mesh != null;
        float posX = particle.lastPosX + (particle.posX - particle.lastPosX) * partialTicks;
        float posY = particle.lastPosY + (particle.posY - particle.lastPosY) * partialTicks;
        float posZ = particle.lastPosZ + (particle.posZ - particle.lastPosZ) * partialTicks;

        Camera camera = ((ClientMinecraft) particle.world.mc).thePlayer.camera;

        double yaw, pitch;
        {
            pitch = -toDegrees(asin(camera.direction.y));
            yaw = toDegrees(atan2(camera.direction.x, camera.direction.z));
        }

        particle.texture.bind();
        {

            shader.pushMatrix();
            shader.translate(posX, posY, posZ);
            shader.rotate(0, 1, 0, (float) yaw);
            shader.rotate(1, 0, 0, (float) pitch);
            particle.mesh.render(shader, GL_TRIANGLES);

            shader.popMatrix();
        }
        shader.enableLighting();
    }
}
