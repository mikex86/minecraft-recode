package me.gommeantilegit.minecraft.world.renderer.graphics.sky;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public class Sun extends Entity {

    /**
     * Sun mesh
     */
    @NotNull
    private final Mesh mesh;

    /**
     * @param world     sets {@link #world}
     */
    public Sun(World world) {
        super(world);
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(Minecraft.mc.vertexAttributes, GL_TRIANGLES);
        meshBuilder.sphere(10, 10, 10, 20, 20);
        this.mesh = meshBuilder.end();
    }

    @Override
    public void tick() {
        setPosition(0, 20, 0);
    }

    @Override
    public void render(float partialTicks) {
//        Gdx.gl.glBindTexture(GL_TEXTURE_2D, 0);
//        StdShader shader = Minecraft.mc.shaderManager.stdShader;
//        shader.pushMatrix();
//        shader.translate(posX, posY, posZ);
//        this.mesh.render(shader, GL_TRIANGLES);
//        shader.popMatrix();
//        super.render(partialTicks);
    }
}
