package me.gommeantilegit.minecraft.world.generation.generator.impl.overworld;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.api.ChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.api.IBlockGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.impl.overworld.generators.impl.surface.SurfaceGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@SideOnly(side = Side.SERVER)
public class WorldChunkGenerator extends ChunkGenerator {

    /**
     * Parent minecraft instance
     */
    @NotNull
    private final ServerMinecraft mc;

    /**
     * Block generators
     */
    @NotNull
    private final IBlockGenerator[] generators;

    /**
     * @param worldGenerator parent world generator to provide random instance and other attributes
     * @param mc             minecraft instance
     */
    public WorldChunkGenerator(@NotNull WorldGenerator worldGenerator, @NotNull ServerMinecraft mc) {
        super(worldGenerator);
        this.generators = getGenerators(worldGenerator.getSeed());
        this.mc = mc;
    }

    @NotNull
    private IBlockGenerator[] getGenerators(long seed) {
        return new IBlockGenerator[]{
                new SurfaceGenerator(seed)
        };
    }

    @Override
    public void onChunkCreated(@NotNull ChunkBase chunk) {
        generateTerrain(worldGenerator.getRandom(), chunk);
    }

    private void generateTerrain(@NotNull Random random, @NotNull ChunkBase chunk) {
        Blocks blocks = mc.getBlocks();
        for (IBlockGenerator generator : this.generators) {
            generator.generateBlocks(random, chunk, blocks);
        }
    }
}
