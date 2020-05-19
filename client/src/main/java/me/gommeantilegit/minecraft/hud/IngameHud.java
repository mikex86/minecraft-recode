package me.gommeantilegit.minecraft.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.font.FontRenderer;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.ui.render.Overlay2D;
import org.jetbrains.annotations.NotNull;

import static me.gommeantilegit.minecraft.utils.MathHelper.humanReadableByteCount;

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

    private String fpsString;
    private int fps = -1, drawCalls = -1;

    @Override
    public void render() {
        int fps = Gdx.graphics.getFramesPerSecond();

        int sectionDrawCalls = mc.theWorld.getWorldRenderer().getChunkSectionDrawCalls();

        if (this.fps != fps || this.drawCalls != sectionDrawCalls) {
            this.fpsString = "FPS: " + fps + "\nSection-Draw-calls: " + sectionDrawCalls + "\nFree memory: " + (Runtime.getRuntime().freeMemory() / (float) Runtime.getRuntime().totalMemory()) * 100 + "%\nFree Memory: " +
                    humanReadableByteCount(Runtime.getRuntime().freeMemory(), true) + "\nTotal Memory: " + humanReadableByteCount(Runtime.getRuntime().totalMemory(), true) +
                    "\nMax memory " + humanReadableByteCount(Runtime.getRuntime().maxMemory(), true) + "\nGrowable: " + humanReadableByteCount(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory(), true) + "\nLoaded chunks: " + mc.theWorld.getWorldChunkHandler().getLoadedChunks().size()
                    + "\nPlayer position: " + mc.thePlayer.getUpdatedPositionVector() + "\nSpeed: " +
                    (Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionY * mc.thePlayer.motionY + mc.thePlayer.motionZ * mc.thePlayer.motionZ) * 20) * 3.6 + " km/h";
            this.drawCalls = sectionDrawCalls;
            this.fps = fps;
        }
        this.mc.uiManager.fontRenderer.drawStringWithShadow(this.fpsString, 0, 0, 1, 1, 1, 1);

        final int selectionCrossSize = 16;
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
    }

}
