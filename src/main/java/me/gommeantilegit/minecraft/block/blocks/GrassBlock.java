package me.gommeantilegit.minecraft.block.blocks;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.block.Block;

public class GrassBlock extends Block {

    public GrassBlock() {
        super("Grass", 3, new String[]{
                "grass_side",
                "grass_side_overlay",
                "grass_top",
                "dirt"
        });
    }

    @Override
    public void renderFace(MeshBuilder builder, int x, int y, int z, int face) {
        Vector2 uv = getUV(face);

        float u0 = uv.x;
        float u1 = uv.x + 16;
        float v0 = uv.y;
        float v1 = uv.y + 16;

        u0 /= TEXTURE_MAP.getWidth();
        u1 /= TEXTURE_MAP.getWidth();
        v0 /= TEXTURE_MAP.getHeight();
        v1 /= TEXTURE_MAP.getHeight();

        float x0 = (float) x + this.xx0;
        float x1 = (float) x + this.xx1;
        float y1 = (float) y + this.yy1;
        float z0 = (float) z + this.zz0;
        float z1 = (float) z + this.zz1;

        float red = 0, green = 1, blue = 0, alpha = 1;

        switch (face) {
            case 1:
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
                        red, green, blue, alpha);
                break;
            case 0:
                super.renderFace(builder, x, y, z, face);
                break;
            default:
                builder.setColor(red, green, blue, alpha);
                super.renderFace(builder, x, y, z, this.textureUVs[1], face);
                builder.setColor(1, 1, 1, 1);
                super.renderFace(builder, x, y, z, face);
                break;
        }
    }

    @Override
    protected Vector2 getUV(int face) {
        switch (face) {
            case 0:
                return this.textureUVs[3];
            case 1:
                return this.textureUVs[2];
            default:
                return this.textureUVs[0];
        }
    }
}
