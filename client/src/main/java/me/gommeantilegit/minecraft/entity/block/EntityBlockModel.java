package me.gommeantilegit.minecraft.entity.block;

import com.badlogic.gdx.graphics.Mesh;
import me.gommeantilegit.minecraft.entity.renderer.model.IEntityModel;
import me.gommeantilegit.minecraft.shader.api.CommonShader;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public class EntityBlockModel implements IEntityModel<CommonShader, EntityBlock> {

    @Override
    public void render(float partialTicks, @NotNull EntityBlock entity, @NotNull CommonShader shader) {
        Mesh mesh = Objects.requireNonNull(entity.getMesh());
        entity.getTexture().bind();

        shader.pushMatrix();
        shader.scale(0.1f, 0.1f, 0.1f);
        mesh.render(shader, GL_TRIANGLES);
        shader.popMatrix();

    }

}
