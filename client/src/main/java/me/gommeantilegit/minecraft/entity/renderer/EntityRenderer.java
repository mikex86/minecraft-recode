package me.gommeantilegit.minecraft.entity.renderer;

import me.gommeantilegit.minecraft.entity.IRenderableEntity;
import me.gommeantilegit.minecraft.entity.particle.Particle;
import me.gommeantilegit.minecraft.entity.player.RenderablePlayer;
import me.gommeantilegit.minecraft.entity.renderer.impl.ParticleRenderer;
import me.gommeantilegit.minecraft.entity.renderer.impl.PlayerRenderer;
import me.gommeantilegit.minecraft.shader.api.CommonShader;
import org.jetbrains.annotations.NotNull;

/**
 * Class for rendering entities
 */
public class EntityRenderer {

    public EntityRenderer() {
        this.playerRenderer = new PlayerRenderer();
        this.particleRenderer = new ParticleRenderer();
    }

    public void renderEntity(@NotNull IRenderableEntity<?, ?> entity, float partialTicks, @NotNull CommonShader shader) {
        if (entity instanceof RenderablePlayer) {
            this.playerRenderer.renderEntity((RenderablePlayer) entity, partialTicks, shader);
        } else if (entity instanceof Particle) {
            this.particleRenderer.renderEntity((Particle) entity, partialTicks, shader);
        }
    }

    /* Individual entity renderer object instances */

    @NotNull
    private final PlayerRenderer playerRenderer;

    @NotNull
    private final ParticleRenderer particleRenderer;
}
