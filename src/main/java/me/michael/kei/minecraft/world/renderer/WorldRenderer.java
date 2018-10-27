package me.michael.kei.minecraft.world.renderer;

import me.michael.kei.minecraft.block.Block;
import me.michael.kei.minecraft.world.World;
import me.michael.kei.minecraft.world.chunk.Chunk;

public class WorldRenderer {

    private final World world;

    public WorldRenderer(World world) {
        this.world = world;
    }

    public void render() {
        Block.TEXTURE_MAP.getTexture().bind();
        for (Chunk chunk : world.getChunks()) {
            chunk.render();
        }
    }

    public World getWorld() {
        return world;
    }
}
