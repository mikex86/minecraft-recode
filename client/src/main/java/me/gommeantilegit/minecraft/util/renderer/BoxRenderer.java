package me.gommeantilegit.minecraft.util.renderer;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.utils.Pointer;
import me.gommeantilegit.minecraft.world.chunk.builder.OptimizedMeshBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.gommeantilegit.minecraft.Side.CLIENT;
import static me.gommeantilegit.minecraft.util.RenderUtils.rect;

@SideOnly(side = CLIENT)
public class BoxRenderer {

    private Vector2[] textureUVs;

    private float xx0, yy0, zz0;
    private float xx1, yy1, zz1;

    /**
     * Pointer to the texture that will be used for rendering the created mesh later.
     * If the pointer points to null, the uv SOUND_RESOURCES of the mesh will be incorrect (they will all be (0,0)), but that will not matter, if you don't plan
     * to use any texture
     */
    @NotNull
    private final Pointer<CustomTexture> texture;

    /**
     * @param textureUVs sets {@link #textureUVs}
     * @param texture    sets {@link #texture}
     */
    public BoxRenderer(@Nullable Vector2[] textureUVs, @NotNull Pointer<CustomTexture> texture) {
        this.textureUVs = textureUVs;
        this.texture = texture;
        this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    public void render(@NotNull OptimizedMeshBuilder builder, int x, int y, int z) {
        renderFace(builder, x, y, z, 0);
        renderFace(builder, x, y, z, 1);
        renderFace(builder, x, y, z, 2);
        renderFace(builder, x, y, z, 3);
        renderFace(builder, x, y, z, 4);
        renderFace(builder, x, y, z, 5);
    }

    /**
     * Setting the shape of the block rendered
     *
     * @param x0 minX
     * @param y0 minY
     * @param z0 minZ
     * @param x1 maxX
     * @param y1 maxY
     * @param z1 maxZ
     */
    public void setShape(float x0, float y0, float z0, float x1, float y1, float z1) {
        this.xx0 = x0;
        this.yy0 = y0;
        this.zz0 = z0;
        this.xx1 = x1;
        this.yy1 = y1;
        this.zz1 = z1;
    }

    /**
     * Renders the specified face into the meshbuilder
     *
     * @param builder the builder for building the chunk's mesh
     * @param x       the x coordinate of the block.
     * @param y       the y coordinate of the block.
     * @param z       the z coordinate of the block.
     * @param face    current face rendered.
     */
    public void renderFace(@NotNull OptimizedMeshBuilder builder, int x, int y, int z, int face) {
        renderFace(builder, x, y, z, getUV(face), face);
    }

    @NotNull
    public Vector2 getUV(int face) {
        return this.textureUVs[0];
    }

    public void renderFace(@NotNull OptimizedMeshBuilder builder, int x, int y, int z, @NotNull Vector2 uv, int face) {
        float u0 = uv.x;
        float u1 = uv.x + 16;
        float v0 = uv.y;
        float v1 = uv.y + 16;

        CustomTexture texture = this.texture.value;
        if (texture != null) {
            u0 /= texture.getWidth();
            u1 /= texture.getWidth();
            v0 /= texture.getHeight();
            v1 /= texture.getHeight();
        } else {
            u0 = 0;
            u1 = 0;
            v0 = 0;
            v1 = 0;
        }
        float x0 = (float) x + this.xx0;
        float x1 = (float) x + this.xx1;
        float y0 = (float) y + this.yy0;
        float y1 = (float) y + this.yy1;
        float z0 = (float) z + this.zz0;
        float z1 = (float) z + this.zz1;

        switch (face) {
            case 0: {
                rect(builder,
                        x1, y0, z1,
                        u1, v1,
                        x1, y0, z0,
                        u1, v0,
                        x0, y0, z0,
                        u0, v0,
                        x0, y0, z1,
                        u0, v1,
                        0, -1, 0,
                        1, 1, 1, 1);
            }
            return;
            case 1: {
                rect(builder,
                        x0, y1, z1,
                        u0, v1,
                        x0, y1, z0,
                        u0, v0,
                        x1, y1, z0,
                        u1, v0,
                        x1, y1, z1,
                        u1, v1,
                        0, 1, 0,
                        1, 1, 1, 1);
                return;
            }
            case 2: {
                rect(builder,
                        x0, y0, z0,
                        u1, v1,
                        x1, y0, z0,
                        u0, v1,
                        x1, y1, z0,
                        u0, v0,
                        x0, y1, z0,
                        u1, v0,
                        0, 0, -1,
                        1, 1, 1, 1);
                return;
            }
            case 3: {
                rect(builder,
                        x1, y1, z1,
                        u1, v0,
                        x1, y0, z1,
                        u1, v1,
                        x0, y0, z1,
                        u0, v1,
                        x0, y1, z1,
                        u0, v0,
                        0, 0, 1,
                        1, 1, 1, 1);
                return;

            }
            case 4: {
                rect(builder,
                        x0, y0, z1,
                        u1, v1,
                        x0, y0, z0,
                        u0, v1,
                        x0, y1, z0,
                        u0, v0,
                        x0, y1, z1,
                        u1, v0,
                        -1, 0, 0,
                        1, 1, 1, 1);
                return;
            }
            case 5: {
                rect(builder,
                        x1, y1, z1,
                        u0, v0,

                        x1, y1, z0,
                        u1, v0,

                        x1, y0, z0,
                        u1, v1,

                        x1, y0, z1,
                        u0, v1,
                        1, 0, 0,
                        1, 1, 1, 1);
            }
        }
    }

    public void setTextureUVs(Vector2[] textureUVs) {
        this.textureUVs = textureUVs;
    }
}
