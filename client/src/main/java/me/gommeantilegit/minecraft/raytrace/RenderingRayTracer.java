package me.gommeantilegit.minecraft.raytrace;

import me.gommeantilegit.minecraft.entity.player.EntityPlayerSP;
import me.gommeantilegit.minecraft.raytrace.render.BlockHighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link RayTracer} inheritance that highlights the traced block
 */
public class RenderingRayTracer extends RayTracer {

    /**
     * Used to highlight the faced block in the {@link #render(RayTraceResult)} method
     */
    @NotNull
    private final BlockHighlighter blockHighlighter;

    /**
     * @param player sets {@link #player}
     */
    public RenderingRayTracer(@NotNull EntityPlayerSP player) {
        super(player, player.mc);
        this.blockHighlighter = new BlockHighlighter(player);
    }

    /**
     * Updating the rayTraceResult + rendering the rayTraceResult by calling {@link #render(RayTraceResult)}
     */
    @Override
    public void update() {
        super.update();
        render(this.getRayTraceResult());
    }

    /**
     * Highlights the block faced ray trace result, if it hits a block
     *
     * @param rayTraceResult the given rayTraceResult
     */
    public void render(@Nullable RayTraceResult rayTraceResult) {
        if (rayTraceResult == null) return;
        if (rayTraceResult.type == RayTraceResult.EnumResultType.BLOCK) {
            assert rayTraceResult.getBlockPos() != null;
            this.blockHighlighter.setBlockPos(rayTraceResult.getBlockPos().getX(), rayTraceResult.getBlockPos().getY(), rayTraceResult.getBlockPos().getZ());
        }
        if (rayTraceResult.type != RayTraceResult.EnumResultType.MISS) {
            this.blockHighlighter.render();
        }
    }
}

