package me.gommeantilegit.minecraft.entity.renderer;

import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.particle.Particle;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.entity.renderer.impl.ParticleRenderer;
import me.gommeantilegit.minecraft.entity.renderer.impl.PlayerRenderer;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import org.jetbrains.annotations.NotNull;

/**
 * Class for rendering entities
 */
public class EntityRenderer implements IEntityRenderer<Entity, StdShader> {

    public EntityRenderer() {
        this.playerRenderer = new PlayerRenderer();
        this.particleRenderer = new ParticleRenderer();
    }

    /**
     * Renders the specified entity
     *
     * @param entity the entity to render
     */
    public void renderEntity(@NotNull Entity entity, float partialTicks, @NotNull StdShader shader) {
        if (entity instanceof PlayerBase) {
            this.playerRenderer.renderEntity((PlayerBase) entity, partialTicks, shader);
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
