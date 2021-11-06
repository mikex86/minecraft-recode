package me.gommeantilegit.minecraft.block.blocks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.BlockTypeRenderer;
import me.gommeantilegit.minecraft.block.texturemap.BlockTextureMap;
import me.gommeantilegit.minecraft.utils.BufferUtils;
import me.gommeantilegit.minecraft.utils.ColorUtils;
import me.gommeantilegit.minecraft.world.chunk.builder.OptimizedMeshBuilder;
import org.jetbrains.annotations.NotNull;

import static me.gommeantilegit.minecraft.util.RenderUtils.rect;

@SideOnly(side = Side.CLIENT)
public class GrassBlockTypeRenderer extends BlockTypeRenderer {

    /**
     * Parent Client minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * Color buffer for grass.
     * Bytes treated as unsigned
     */
    private static final byte @NotNull [] grassBuffer = BufferUtils.toArray(new Pixmap(Gdx.files.classpath("textures/colormap/grass.png")).getPixels());

    /**
     * Grass overlay tint color
     */
    @NotNull
    private static final Color COLOR = new Color(getGrassColor(1, 1));

    public GrassBlockTypeRenderer(@NotNull Block grassBlock, @NotNull ClientMinecraft mc) {
        super(grassBlock, mc,
                "dirt",
                "grass_side_overlay",
                "grass_top"
        );
        this.mc = mc;
    }

    @Override
    public void renderFace(@NotNull OptimizedMeshBuilder builder, int x, int y, int z, int face) {
        switch (face) {
            case 1: // top
                builder.setColor(COLOR.r, COLOR.g, COLOR.b, 1);
                super.renderFace(builder, x, y, z, this.textureUVs[2], face);
                builder.setColor(1, 1, 1, 1);
                break;
            case 0: // bottom
                super.renderFace(builder, x, y, z, face);
                break;
            default: // side
                super.renderFace(builder, x, y, z, this.textureUVs[0], face);
                builder.setColor(COLOR.r, COLOR.g, COLOR.b, 1);
                super.renderFace(builder, x, y, z, this.textureUVs[1], face);
                builder.setColor(1, 1, 1, 1);
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

    /**
     * Gets grass color from temperature and humidity
     *
     * @param temperature 0.0 - 1.0 value
     * @param humidity    0.0 - 1.0 value
     * @return the rgba value for the grass tint
     */
    public static int getGrassColor(double temperature, double humidity) {
        humidity = humidity * temperature;
        int i = (int) ((1.0 - temperature) * 255.0);
        int j = (int) ((1.0 - humidity) * 255.0);
        int k = j << 8 | i;
        k *= 4;
        return k > grassBuffer.length ? 0xffff00ff : ColorUtils.toRGBA(grassBuffer[k], grassBuffer[k + 1], grassBuffer[k + 2], grassBuffer[k + 3]);
    }
}
