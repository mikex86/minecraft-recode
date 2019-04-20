package me.gommeantilegit.minecraft.entity.renderer.model;

import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.shader.api.CommonShader;
import org.jetbrains.annotations.NotNull;

public interface IEntityModel<T extends Entity, S extends CommonShader> {

    void render(float partialTicks, @NotNull T entity, @NotNull S shader);

}
