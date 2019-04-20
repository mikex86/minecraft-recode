package me.gommeantilegit.minecraft.world.generation.generator;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.api.INBTConverter;
import me.gommeantilegit.minecraft.nbt.exception.NBTParsingException;
import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.nbt.impl.NBTInteger;
import me.gommeantilegit.minecraft.nbt.impl.NBTLong;
import me.gommeantilegit.minecraft.nbt.impl.NBTStringMap;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
import me.gommeantilegit.minecraft.world.generation.generator.api.ChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.impl.overworld.WorldChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.impl.superflat.SuperFlatChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.options.WorldGenerationOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@SideOnly(side = Side.SERVER)
public class WorldGenerator implements ServerWorld.OnServerChunkCreationListener {

    /**
     * NBT Converter for {@link WorldGenerator} object
     */
    @NotNull
    public static final NBTConverter NBT_CONVERTER = new NBTConverter();

    /**
     * Random instance for creator
     */
    @NotNull
    private final Random random;

    /**
     * ServerWorld type enum value
     */
    @NotNull
    private final WorldType worldType;

    /**
     * ServerWorld Generation seed
     */
    private final long seed;

    @NotNull
    private final ChunkGenerator chunkGenerator;

    /**
     * Server Minecraft instance
     */
    @NotNull
    private final ServerMinecraft mc;

    /**
     * Options regarding world creator
     */
    @NotNull
    private final WorldGenerationOptions worldGenerationOptions;

    public WorldGenerator(long seed, @NotNull WorldType worldType, @NotNull ServerMinecraft mc, @NotNull WorldGenerationOptions worldGenerationOptions) {
        this.seed = seed;
        this.random = new Random(seed);
        this.worldType = worldType;
        this.mc = mc;
        this.worldGenerationOptions = worldGenerationOptions;
        switch (worldType) {
            case SUPER_FLAT:
                chunkGenerator = new SuperFlatChunkGenerator(this, this.mc);
                break;
            case OVERWORLD:
                chunkGenerator = new WorldChunkGenerator(this, this.mc);
                break;
            default:
                throw new IllegalStateException("Invalid world type: " + worldType);
        }
    }

    @Override
    public void onChunkCreated(@NotNull ServerChunk chunk) {
        this.chunkGenerator.onChunkCreated(chunk);
    }

    @NotNull
    public WorldType getWorldType() {
        return worldType;
    }

    @NotNull
    public ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }

    @NotNull
    public Random getRandom() {
        return random;
    }

    public long getSeed() {
        return seed;
    }

    @NotNull
    public WorldGenerationOptions getWorldGenerationOptions() {
        return worldGenerationOptions;
    }

    public enum WorldType {
        OVERWORLD, SUPER_FLAT
    }

    public static final class NBTConverter implements INBTConverter<NBTArray, WorldGenerator> {
        @NotNull
        @Override
        public NBTArray toNBTData(@NotNull WorldGenerator object) {
            return new NBTArray(new NBTObject[]{
                    new NBTLong(object.seed),
                    new NBTInteger(object.worldType.ordinal()),
                    WorldGenerationOptions.NBT_CONVERTER.toNBTData(object.worldGenerationOptions)
            });
        }

        @Override
        public WorldGenerator fromNBTData(NBTArray object, Object... args) throws NBTParsingException {
            long seed = ((NBTLong) object.getValue()[0]).getValue();
            WorldType worldType = WorldType.values()[((NBTInteger) object.getValue()[1]).getValue()];
            WorldGenerationOptions options = WorldGenerationOptions.NBT_CONVERTER.fromNBTData(((NBTStringMap) object.getValue()[2]));
            return new WorldGenerator(seed, worldType, (ServerMinecraft) args[0], options);
        }
    }
}
