package me.gommeantilegit.minecraft.block.render;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.ClientBlock;
import me.gommeantilegit.minecraft.util.renderer.BoxRenderer;
import org.jetbrains.annotations.NotNull;

import static me.gommeantilegit.minecraft.Side.CLIENT;

@SideOnly(side = CLIENT)
public class BlockRenderer extends BoxRenderer {

    /**
     * The client side asserted block type to render
     */
    @NotNull
    private final ClientBlock block;

    public BlockRenderer(@NotNull ClientBlock block) {
        super(null, block.mc.textureManager.blockTextureMap.getTexturePointer());
        this.block = block;
    }

    @Override
    @NotNull
    public Vector2 getUV(int face) {
        return block.getUV(face);
    }
}
