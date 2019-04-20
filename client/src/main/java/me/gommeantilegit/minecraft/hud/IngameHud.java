package me.gommeantilegit.minecraft.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.font.FontRenderer;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.ui.render.Overlay2D;
import org.jetbrains.annotations.NotNull;

public class IngameHud extends Overlay2D {

    @NotNull
    private final SpriteBatch spriteBatch;

    @NotNull
    private final ClientMinecraft mc;

    /**
     * @param spriteBatch spritebatch instance for 2d rendering
     */
    public IngameHud(@NotNull SpriteBatch spriteBatch, @NotNull ClientMinecraft mc) {
        super(spriteBatch);
        this.spriteBatch = spriteBatch;
        this.mc = mc;
    }

    private String fpsString = "FPS: " + Gdx.graphics.getFramesPerSecond();
    private int fps;

    @Override
    public void render() {
        int fps = Gdx.graphics.getFramesPerSecond();
        if (this.fps != fps) {
            fpsString = "FPS: " + fps;
            this.fps = fps;
        }
        mc.uiManager.fontRenderer.drawStringWithShadow(fpsString, 0, 0, 1, 1, 1, 1);

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

}
