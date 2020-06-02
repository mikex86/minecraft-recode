package me.gommeantilegit.minecraft.entity.renderer;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.IRenderableEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Global Object used to render a given entity
 *
 * @param <T> Type of the entity to be rendered
 */
public interface IEntityRenderer<S extends ShaderProgram, T extends IRenderableEntity<S, T>> {

    /**
     * Renders the given entity to the screen
     *
     * @param entity        the given entity
     * @param partialTicks  timer partialTicks
     * @param shaderProgram the shader program
     */
    void renderEntity(@NotNull T entity, float partialTicks, @NotNull S shaderProgram);

}
