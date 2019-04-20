package me.gommeantilegit.minecraft.block.blocks;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.ClientBlock;
import me.gommeantilegit.minecraft.block.ClientBlocks;
import me.gommeantilegit.minecraft.block.texturemap.BlockTextureMap;
import org.jetbrains.annotations.NotNull;

import static me.gommeantilegit.minecraft.util.RenderUtils.rect;

@SideOnly(side = Side.CLIENT)
public class ClientGrassBlock extends ClientBlock {

//    /**
//     * Color buffer for grass.
//     * Bytes treated as unsigned
//     */
//    private static byte[] grassBuffer = BufferUtils.toArray(new Pixmap(Gdx.files.classpath("textures/colormap/grass.png")).getPixels());
//
//    /**
//     * Grass overlay tint color
//     */
//    @NotNull
//    private static final Color COLOR = new Color(getGrassColor(0.45, 0.15));

    public ClientGrassBlock(@NotNull ClientBlocks blocks, @NotNull ClientMinecraft mc) {
        super("Grass", (short)3, new String[]{
                "dirt",
                "grass_side",
                "grass_side_overlay",
                "grass_top_old"
        }, true, blocks, mc, false);
    }

    @Override
    public void renderFace(@NotNull MeshBuilder builder, int x, int y, int z, int face) {
        Vector2 uv = getUV(face);

        float u0 = uv.x;
        float u1 = uv.x + 16;
        float v0 = uv.y;
        float v1 = uv.y + 16;

        BlockTextureMap map = mc.textureManager.blockTextureMap;

        u0 /= map.getWidth();
        u1 /= map.getWidth();
        v0 /= map.getHeight();
        v1 /= map.getHeight();

        float x0 = (float) x + this.x0;
        float x1 = (float) x + this.x1;
        float y1 = (float) y + this.y1;
        float z0 = (float) z + this.z0;
        float z1 = (float) z + this.z1;

        builder.setColor(1, 1, 1, 1);
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
                        1, 1, 1, 1);
                break;
            case 0:
                super.renderFace(builder, x, y, z, face);
                break;
            default:
                super.renderFace(builder, x, y, z, this.textureUVs[1], face);
//                super.renderFace(builder, x, y, z, this.textureUVs[2], face);
                break;
        }
    }

    @NotNull
    @Override
    public Vector2 getUV(int face) {
        switch (face) {
            case 0:
                return this.textureUVs[0];
            case 1:
                return this.textureUVs[3];
            default:
                return this.textureUVs[0];
        }
    }

//    /**
//     * Gets grass color from temperature and humidity
//     *
//     * @param temperature 0.0 - 1.0 value
//     * @param humidity    0.0 - 1.0 value
//     * @return the rgba value for the grass tint
//     */
//    public static int getGrassColor(double temperature, double humidity) {
//        humidity = humidity * temperature;
//        int i = (int) ((1.0 - temperature) * 255.0);
//        int j = (int) ((1.0 - humidity) * 255.0);
//        int k = j << 8 | i;
//        k *= 4;
//        return k > grassBuffer.length ? 0xffff00ff : ColorUtils.toRGBA(grassBuffer[k], grassBuffer[k + 1], grassBuffer[k + 2], grassBuffer[k + 3]);
//    }

}
