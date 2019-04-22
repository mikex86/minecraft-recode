package me.gommeantilegit.minecraft.raytrace.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerSP;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.utils.Pointer;
import me.gommeantilegit.minecraft.util.renderer.BoxRenderer;
import org.jetbrains.annotations.NotNull;

import static com.badlogic.gdx.graphics.GL20.*;
import static java.lang.Math.min;
import static me.gommeantilegit.minecraft.rendering.Constants.STD_VERTEX_ATTRIBUTES;

/**
 * Client only
 */
@SideOnly(side = Side.CLIENT)
public class BlockHighlighter {

    /**
     * Positional components of the block being highlighted
     */
    private int blockX, blockY, blockZ;

    /**
     * Block mesh
     */
    @NotNull
    private final Mesh linesMesh;

    @NotNull
    private final Mesh trianglesMesh;

    /**
     * Parent player
     */
    @NotNull
    private final EntityPlayerSP player;

    /**
     * Minecraft instance asserted to be client side
     */
    @NotNull
    private final ClientMinecraft mc;

    public BlockHighlighter(@NotNull EntityPlayerSP player) {
        this.mc = player.mc;

        this.player = player;
        //Lines mesh
        {
            MeshBuilder meshBuilder = new MeshBuilder();
            meshBuilder.setColor(1, 0, 0, 1); // White color
            meshBuilder.begin(STD_VERTEX_ATTRIBUTES, GL20.GL_LINES);
            {
                float xo = 0.5f, yo = 0.5f, zo = 0.5f;
                float offset = 2.0E-03f;
                meshBuilder.box(xo + offset, yo + offset, zo + offset, 1 + 2 * offset, 1 + 2 * offset, 1 + 2 * offset);
            }
            this.linesMesh = meshBuilder.end();
        }
        //Triangles mesh
        {
            MeshBuilder meshBuilder = new MeshBuilder();
            meshBuilder.setColor(1, 0, 0, 1);
            meshBuilder.begin(STD_VERTEX_ATTRIBUTES, GL20.GL_TRIANGLES);
            {
                createBlockMesh(meshBuilder);
            }
            this.trianglesMesh = meshBuilder.end();
        }
    }

    private void createBlockMesh(@NotNull MeshBuilder meshBuilder) {
        BoxRenderer renderer = new BoxRenderer(new Vector2[]{
                new Vector2(0, 0)
        }, new Pointer<>(mc.textureManager.blockDestroyStages[0]));
        renderer.render(meshBuilder, 0, 0, 0);
    }

    /**
     * Sets the value of the position vector defining the block being highlighted
     *
     * @param blockX sets {@link #blockX}
     * @param blockY sets {@link #blockY}
     * @param blockZ sets {@link #blockZ}
     */
    public void setBlockPos(int blockX, int blockY, int blockZ) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
    }

    /**
     * Renders the mesh highlighting the given block specified using {@link #setBlockPos(int, int, int)}
     * Note:
     * - Changes line-width to 4. Resets it to 1 afterwards.
     */
    public void render() {
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        StdShader shader = mc.shaderManager.stdShader;
        shader.pushMatrix();
        shader.translate(blockX, blockY, blockZ);
        {

            Gdx.gl.glLineWidth(2);
            shader.forceColor(0f, 0f, 0f, 0.4f);
            this.linesMesh.render(shader, GL20.GL_LINES);
            shader.resetForcedColor();
            Gdx.gl.glLineWidth(1);

            Gdx.gl.glEnable(GL_POLYGON_OFFSET_FILL);
            Gdx.gl.glPolygonOffset(-3, -3);
            if (player.playerController.currentBlockDamage > 0) {
                this.renderBlockBreakingDamage();
            }
            Gdx.gl.glDisable(GL_POLYGON_OFFSET_FILL);
        }
        shader.popMatrix();
    }

    /**
     * Renders the block breaking progress
     */
    private void renderBlockBreakingDamage() {
        StdShader shader = mc.shaderManager.stdShader;
        Gdx.gl.glBlendFunc(GL_DST_COLOR, GL_SRC_COLOR);
        float blockDamage = min(mc.thePlayer.playerController.currentBlockDamage, 0.999999f);
        mc.textureManager.blockDestroyStages[(int) (blockDamage * 10f)].bind();
        shader.disableLighting();
        this.trianglesMesh.render(shader, GL20.GL_TRIANGLES);
        shader.enableLighting();
        shader.setColor(1, 1, 1, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

}
