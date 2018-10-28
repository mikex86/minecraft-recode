package me.gommeantilegit.minecraft.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.Minecraft;
import org.jetbrains.annotations.Nullable;

import static org.lwjgl.opengl.GL11.*;

public class Cube {

    /**
     * Size of the rendered cube.
     */
    public final float width, height, depth;
    /**
     * Texture pixel region sizes.
     */
    private final float textureWidth, textureHeight, textureDepth;
    private final Texture texture;

    private int textureOffsetX0, textureOffsetY0;

    public float x, y, z;
    public float xRot, yRot, zRot;
    private Mesh mesh;
    /**
     * Helper variable used for scaling an uncompiled mesh after it's compiled.
     */
    @Nullable
    private Vector3 scaleTo;

    public Cube(int textureOffsetX0,
                int textureOffsetY0,
                float x, float y, float z, float width, float height, float depth, float textureWidth, float textureHeight, float textureDepth, Texture texture) {
        this.texture = texture;
        this.textureOffsetX0 = textureOffsetX0;
        this.textureOffsetY0 = textureOffsetY0;
        this.textureDepth = textureDepth;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public void render(Camera camera, ShaderProgram shaderProgram) {
        if (mesh == null) {
            mesh = compile();
            if (this.scaleTo != null) mesh.scale(scaleTo.x, scaleTo.y, scaleTo.z);
        }
        texture.bind();

        camera.combined.rotate(1, 0, 0, xRot);
        camera.combined.rotate(0, 1, 0, yRot);
        camera.combined.rotate(0, 0, 1, zRot);

        shaderProgram.setUniformMatrix("matViewProj", camera.combined);
        mesh.render(shaderProgram, GL_TRIANGLES);

        camera.combined.rotate(0, 0, 1, -zRot);
        camera.combined.rotate(0, 1, 0, -yRot);
        camera.combined.rotate(1, 0, 0, -xRot);

        shaderProgram.setUniformMatrix("matViewProj", camera.combined);
    }

    public void scale(float x, float y, float z) {
        if (mesh != null) {
            this.mesh.scale(x, y, z);
        } else {
            this.scaleTo = new Vector3(x, y, z);
        }
    }

    private Mesh compile() {
        MeshBuilder builder = new MeshBuilder();
        builder.begin(Minecraft.mc.vertexAttributes, GL_TRIANGLES);
        for (int face = 0; face <= 5; face++) {
            renderFace(face, builder);
        }
        return builder.end();
    }

    private void renderFace(int face, MeshBuilder builder) {

        float x0 = x, y0 = y, z0 = z;
        float x1 = x + width, y1 = y + height, z1 = z + depth;

        switch (face) {
            case 0: {

                float u0 = (this.textureOffsetX0 + this.textureDepth + this.textureWidth) / this.texture.getWidth();
                float u1 = (this.textureOffsetX0 + this.textureDepth + this.textureWidth + this.textureDepth) / this.texture.getWidth();
                float v0 = (float) this.textureOffsetY0 / this.texture.getHeight();
                float v1 = (this.textureOffsetY0 + this.textureDepth) / this.texture.getHeight();

                rect(builder,
                        x1, y0, z1,
                        u1, v1,

                        x1, y0, z0,
                        u1, v0,

                        x0, y0, z0,
                        u0, v0,

                        x0, y0, z1,
                        u0, v1,
                        0, -1, 0);
            }
            return;

            case 1: {

                float u0 = (this.textureOffsetX0 + this.textureDepth) / this.texture.getWidth();
                float u1 = (this.textureOffsetX0 + this.textureDepth + this.textureWidth) / this.texture.getWidth();
                float v0 = (float) this.textureOffsetY0 / this.texture.getHeight();
                float v1 = (this.textureOffsetY0 + this.textureDepth) / this.texture.getHeight();

                rect(builder,
                        x0, y1, z1,
                        u0, v1,

                        x0, y1, z0,
                        u0, v0,

                        x1, y1, z0,
                        u1, v0,

                        x1, y1, z1,
                        u1, v1,
                        0, 1, 0);
                return;
            }
            case 2: {

                float u0 = (this.textureOffsetX0 + this.textureWidth + this.textureDepth * 2) / this.texture.getWidth();
                float u1 = (this.textureOffsetX0 + this.textureWidth * 2 + this.textureDepth * 2) / this.texture.getWidth();
                float v0 = (this.textureOffsetY0 + this.textureDepth) / this.texture.getHeight();
                float v1 = (this.textureOffsetY0 + this.textureDepth + this.textureHeight) / this.texture.getHeight();

                rect(builder,
                        x0, y0, z0,
                        u0, v1,

                        x1, y0, z0,
                        u1, v1,

                        x1, y1, z0,
                        u1, v0,

                        x0, y1, z0,
                        u0, v0,
                        0, 0, -1);
                return;
            }
            case 3: {

                float u0 = (this.textureOffsetX0 + this.textureDepth) / this.texture.getWidth();
                float u1 = (this.textureOffsetX0 + this.textureDepth + this.textureWidth) / this.texture.getWidth();
                float v0 = (this.textureOffsetY0 + this.textureDepth) / this.texture.getHeight();
                float v1 = (this.textureOffsetY0 + this.textureDepth + this.textureHeight) / this.texture.getHeight();

                rect(builder,
                        x1, y1, z1,
                        u1, v0,
                        x1, y0, z1,
                        u1, v1,
                        x0, y0, z1,
                        u0, v1,
                        x0, y1, z1,
                        u0, v0,
                        0, 0, 1);
                return;

            }
            case 4: {

                float u0 = (float) (this.textureOffsetX0) / this.texture.getWidth();
                float u1 = (this.textureOffsetX0 + this.textureDepth) / this.texture.getWidth();
                float v0 = (this.textureOffsetY0 + this.textureDepth) / this.texture.getHeight();
                float v1 = (this.textureOffsetY0 + this.textureDepth + this.textureHeight) / this.texture.getHeight();

                rect(builder,
                        x0, y0, z1,
                        u1, v1,
                        x0, y0, z0,
                        u0, v1,
                        x0, y1, z0,
                        u0, v0,
                        x0, y1, z1,
                        u1, v0,
                        -1, 0, 0);
                return;
            }
            case 5: {

                float u0 = (this.textureOffsetX0 + this.textureWidth + this.textureDepth) / this.texture.getWidth();
                float u1 = (this.textureOffsetX0 + this.textureWidth + this.textureDepth * 2) / this.texture.getWidth();
                float v0 = (this.textureOffsetY0 + this.textureDepth) / this.texture.getHeight();
                float v1 = (this.textureOffsetY0 + this.textureDepth + this.textureHeight) / this.texture.getHeight();

                rect(builder,
                        x1, y1, z1,
                        u0, v0,

                        x1, y1, z0,
                        u1, v0,

                        x1, y0, z0,
                        u1, v1,

                        x1, y0, z1,
                        u0, v1,
                        1, 0, 0);
            }
        }
    }

    protected void rect(MeshBuilder builder,
                        float x00, float y00, float z00,
                        float u00, float v00,
                        float x10, float y10, float z10,
                        float u10, float v10,
                        float x11, float y11, float z11,
                        float u11, float v11,
                        float x01, float y01, float z01,
                        float u01, float v01,
                        float normalX, float normalY, float normalZ) {

        builder.rect(
                new MeshPartBuilder.VertexInfo().setPos(x00, y00, z00).setNor(normalX, normalY, normalZ).setUV(u00, v00),
                new MeshPartBuilder.VertexInfo().setPos(x10, y10, z10).setNor(normalX, normalY, normalZ).setUV(u10, v10),
                new MeshPartBuilder.VertexInfo().setPos(x11, y11, z11).setNor(normalX, normalY, normalZ).setUV(u11, v11),
                new MeshPartBuilder.VertexInfo().setPos(x01, y01, z01).setNor(normalX, normalY, normalZ).setUV(u01, v01)
        );

    }

    public void translate(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }
}

