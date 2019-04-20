package me.gommeantilegit.minecraft.block.render;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.BlockBase;
import me.gommeantilegit.minecraft.block.BlockTypeRenderer;
import me.gommeantilegit.minecraft.util.renderer.BoxRenderer;
import org.jetbrains.annotations.NotNull;

import static me.gommeantilegit.minecraft.Side.CLIENT;

@SideOnly(side = CLIENT)
public class BlockRenderer extends BoxRenderer {

    /**
     * The client side asserted block type to render
     */
    @NotNull
    private final BlockBase block;

    /**
     * The block type specific render object
     */
    @NotNull
    private final BlockTypeRenderer blockTypeRenderer;

    public BlockRenderer(@NotNull BlockBase block, @NotNull ClientMinecraft mc, @NotNull BlockTypeRenderer blockTypeRenderer) {
        super(null, mc.textureManager.blockTextureMap.getTexturePointer());
        this.block = block;
        this.blockTypeRenderer = blockTypeRenderer;
    }

    @Override
    @NotNull
    public Vector2 getUV(int face) {
        return blockTypeRenderer.getUV(face);
    }
}
