package me.gommeantilegit.minecraft.world.renderer;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.chunk.Chunk;

public class WorldRenderer {

    /**
     * Parent world to store
     */
    private final World world;

    public WorldRenderer(World world) {
        this.world = world;
    }

    /**
     * Renders the world
     * @param partialTicks delta time
     */
    public void render(float partialTicks) {
        //Rendering Blocks.
        {
            Block.TEXTURE_MAP.getTexture().bind();
            for (Chunk chunk : world.genChunks()) {
                chunk.render();
            }
        }
        //Rendering Entities
        renderEntities(partialTicks);
    }

    /**
     * Renders all entities
     * @param partialTicks delta time
     */
    private void renderEntities(float partialTicks) {
        for (Entity entity : world.entities) {
            entity.render(partialTicks);
        }
    }

    public World getWorld() {
        return world;
    }
}
