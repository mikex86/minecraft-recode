package me.gommeantilegit.minecraft.entity;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.gommeantilegit.minecraft.entity.renderer.model.IEntityModel;
import org.jetbrains.annotations.NotNull;

public interface IRenderableEntity<
        S extends ShaderProgram,
        T extends IRenderableEntity<S, T> // self
        > {

    @NotNull IEntityModel<S, T> getModel();

}
