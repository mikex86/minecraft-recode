package me.gommeantilegit.minecraft.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import me.gommeantilegit.minecraft.profiler.Profiler;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import org.jetbrains.annotations.NotNull;

import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.font.FontRenderer;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.ui.render.Overlay2D;

import static java.lang.Math.floor;
import static java.lang.Math.round;
import static me.gommeantilegit.minecraft.util.MathHelper.floorCeil;
import static me.gommeantilegit.minecraft.util.MathHelper.humanReadableByteCount;
import static me.gommeantilegit.minecraft.world.chunk.Chunk.CHUNK_SIZE;

public class IngameHud extends Overlay2D {

    @NotNull
    public final Minecraft mc = Minecraft.mc;

    @NotNull
    public final FontRenderer fontRenderer;

    @NotNull
    private final SpriteBatch spriteBatch;

    /**
     * Scaled width and height for rendering
     */
    private float scaledWidth, scaledHeight;

    /**
     * Scale vector
     */
    public float scaleX, scaleY;

    /**
     * @param spriteBatch spritebatch instance for 2d rendering
     */
    public IngameHud(@NotNull SpriteBatch spriteBatch) {
        super(spriteBatch);
        this.spriteBatch = spriteBatch;
        this.fontRenderer = new FontRenderer(this.spriteBatch);
    }

    @Override
    public void render() {
        fontRenderer.drawString("FPS: " + Gdx.graphics.getFramesPerSecond(), 0, 0, 1, 1, 1, 1);

        final int selectionCrossSize = 32;
        this.mc.textureManager.hudTextures.drawSelectionCross(
                DPI.scaledWidthi / 2 - selectionCrossSize / 2,
                DPI.scaledHeighti / 2 - selectionCrossSize / 2, selectionCrossSize);
//        {
//
//            float x = mc.thePlayer.posX, z = mc.thePlayer.posZ;
//            int xi = floorCeil(x), zi = floorCeil(z);
//
//            Vec2i pos = mc.theWorld.getChunkOrigin(x, z);
//            fontRenderer.drawString("ChunkX: " + pos.getX() + ", ChunkZ: " + pos.getY(), 0, 20, 1, 1, 1, 1);
//            fontRenderer.drawString("PosXi: " + xi + ", PosZi: " + zi, 0, 40, 1, 1, 1, 1);
//            fontRenderer.drawString(String.format("PosXd: %.3f, PosZd: %.3f", x, z), 0, 60, 1, 1, 1, 1);
//
//        }
//        {
//            RayTracer.RayTraceResult result = mc.thePlayer.rayTracer.rayTraceResult;
//            fontRenderer.drawString("Hitresult: x: " + result.blockX + ", y: " + result.blockY + ", z: " + result.blockZ, 0, 80, 1, 1, 1, 1);
//        }
//        {
//            fontRenderer.drawString("MemoryUsage: " + (Runtime.getRuntime().freeMemory() / (float) Runtime.getRuntime().maxMemory()) * 100 + "% " +
//                    humanReadableByteCount(Runtime.getRuntime().freeMemory(), true) + " used of " + humanReadableByteCount(Runtime.getRuntime().maxMemory(), true), 0, 100, 1, 1, 1, 1);
//
//        }
    }

    /**
     * Updating frame buffer with and height og the sprite-batch
     *
     * @param width  the new width
     * @param height the new height
     */
    public void resize(int width, int height) {
        DPI.update();
        this.scaleX = DPI.scaleX;
        this.scaleY = DPI.scaleY;
        this.scaledWidth = DPI.scaledWidth;
        this.scaledHeight = DPI.scaledHeight;
        this.spriteBatch.getProjectionMatrix().setToOrtho(0, scaledWidth, scaledHeight, 0, 0, 1);
    }

}
