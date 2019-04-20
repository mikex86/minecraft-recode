package me.gommeantilegit.minecraft.block;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.block.blocks.GrassBlockTypeRenderer;
import me.gommeantilegit.minecraft.block.render.BlockRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;

public class ClientBlockRendererTypeRegistry {

    /**
     * Parent minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * The registry storing the block and it's parent renderer
     */
    @NotNull
    private final IdentityHashMap<BlockBase, BlockTypeRenderer> rendererRegistry = new IdentityHashMap<>();

    /**
     * The parent blocks instance
     */
    @NotNull
    private final Blocks blocks;

    public ClientBlockRendererTypeRegistry(@NotNull ClientMinecraft mc, @NotNull Blocks blocks) {
        this.mc = mc;
        this.blocks = blocks;
    }

    public void init() {
        rendererRegistry.put(blocks.stone, new BlockTypeRenderer(blocks.stone, mc, "stone"));
        rendererRegistry.put(blocks.dirt, new BlockTypeRenderer(blocks.stone, mc, "dirt"));
        rendererRegistry.put(blocks.grass, new GrassBlockTypeRenderer(blocks.grass, mc));
        rendererRegistry.put(blocks.bedrock, new BlockTypeRenderer(blocks.bedrock, mc, "bedrock"));
        rendererRegistry.put(blocks.quartz, new BlockTypeRenderer(blocks.quartz, mc, "quartz_block_bottom"));
    }

    public void buildTextureMap() {
        mc.textureManager.blockTextureMap.build(); // Building the block texture map
    }

    /**
     * @param blockBase the block instance
     * @return the renderer instance for the specified block
     */
    @Nullable
    public BlockTypeRenderer getRenderer(@NotNull BlockBase blockBase) {
        return rendererRegistry.get(blockBase);
    }

    @NotNull
    public IdentityHashMap<BlockBase, BlockTypeRenderer> getRendererRegistry() {
        return rendererRegistry;
    }
}
