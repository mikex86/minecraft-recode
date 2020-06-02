package me.gommeantilegit.minecraft.entity.renderer.model;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.gommeantilegit.minecraft.entity.IRenderableEntity;
import org.jetbrains.annotations.NotNull;

public interface IEntityModel<S extends ShaderProgram, T extends IRenderableEntity<S, T>> {

    void render(float partialTicks, @NotNull T entity, @NotNull S shader);

}
