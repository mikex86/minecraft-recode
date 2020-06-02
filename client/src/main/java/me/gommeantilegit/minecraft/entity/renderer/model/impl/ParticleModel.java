package me.gommeantilegit.minecraft.entity.renderer.model.impl;

import com.badlogic.gdx.graphics.Mesh;
import me.gommeantilegit.minecraft.entity.particle.Particle;
import me.gommeantilegit.minecraft.entity.renderer.model.IEntityModel;
import me.gommeantilegit.minecraft.shader.api.CommonShader;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public class ParticleModel implements IEntityModel<CommonShader, Particle> {

    @Override
    public void render(float partialTicks, @NotNull Particle particle, @NotNull CommonShader shader) {
        Mesh mesh = Objects.requireNonNull(particle.getMesh());
        particle.getTexture().bind();
        mesh.render(shader, GL_TRIANGLES);
    }

}
