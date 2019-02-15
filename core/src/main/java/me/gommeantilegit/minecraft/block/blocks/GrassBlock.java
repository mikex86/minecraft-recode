package me.gommeantilegit.minecraft.block.blocks;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Vector2;

import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.texturemap.BlockTextureMap;

public class GrassBlock extends Block {

    public GrassBlock() {
        super("Grass", 3, new String[]{
                "dirt",
                "grass_side",
                "grass_side_overlay",
                "grass_top"
        }, true);
    }

    @Override
    public void renderFace(MeshBuilder builder, int x, int y, int z, int face) {
        Vector2 uv = getUV(face);

        float u0 = uv.x;
        float u1 = uv.x + 16;
        float v0 = uv.y;
        float v1 = uv.y + 16;

        BlockTextureMap map = Minecraft.mc.textureManager.blockTextureMap;
        u0 /= map.getWidth();
        u1 /= map.getWidth();
        v0 /= map.getHeight();
        v1 /= map.getHeight();

        float x0 = (float) x + this.x0;
        float x1 = (float) x + this.x1;
        float y1 = (float) y + this.y1;
        float z0 = (float) z + this.z0;
        float z1 = (float) z + this.z1;

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
                builder.setColor(1, 1, 1, 1);
                super.renderFace(builder, x, y, z, face);
                builder.setColor(red, green, blue, alpha);
                super.renderFace(builder, x, y, z, this.textureUVs[2], face);
                builder.setColor(1, 1, 1, 1);
                break;
        }
    }

    @Override
    protected Vector2 getUV(int face) {
        switch (face) {
            case 0:
                return this.textureUVs[0];
            case 1:
                return this.textureUVs[3];
            default:
                return this.textureUVs[0];
        }
    }
}
