package me.gommeantilegit.minecraft.world.saveformat;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.gmcdata.GMC;
import me.gommeantilegit.minecraft.gmcdata.SerializedProperty;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.options.WorldGenerationOptions;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.BitSet;

@SideOnly(side = Side.SERVER)
public class WorldLoader {

    @NotNull
    private final ServerMinecraft mc;

    /**
     * The world directory containing all the world files (and chunk data)
     */
    @NotNull
    private final File worldDir;

    /**
     * The file containing the serialized {@link WorldGenerationOptions}
     */
    @NotNull
    private final File worldGenOptionsFile;

    /**
     * The file containing the serialized {@link WorldOptions}
     */
    @NotNull
    private final File worldOptionsFile;

    /**
     * The directory storing chunks data
     */
    @NotNull
    private final File chunksDirectory;

    @NotNull
    private final GMC gmc = new GMC();

    /**
     * @param worldDir the world directory
     * @param mc       the Minecraft instance
     */
    public WorldLoader(@NotNull File worldDir, @NotNull ServerMinecraft mc) {
        this.mc = mc;
        this.worldDir = worldDir;
        this.worldGenOptionsFile = new File(worldDir, "world_gen_options.gmc");
        this.worldOptionsFile = new File(worldDir, "world_options.gmc");
        this.chunksDirectory = new File(worldDir, "chunks");
    }

    /**
     * @return the created world instance
     */
    @NotNull
    public ServerWorld loadWorld() throws IOException {
        WorldGenerationOptions generationOptions = this.gmc.fromGMC(new BufferedInputStream(new FileInputStream(this.worldGenOptionsFile)), WorldGenerationOptions.class);
        WorldOptions worldOptions = this.gmc.fromGMC(new BufferedInputStream(new FileInputStream(this.worldOptionsFile)), WorldOptions.class);
        ServerWorld world = new ServerWorld(this.mc, new WorldGenerator(mc, generationOptions), this.worldDir, worldOptions.getHeight(), this.mc.getBlocks().getGlobalPalette());
        world.setSpawnPoint(worldOptions.getSpawnPoint());
        world.addOnChunkCreationListener(WorldLoader.this::onChunkCreation);
        return world;
    }

    /**
     * Called on chunk creation
     * Restores the chunk to it's saved state
     *
     * @param chunk the given chunk
     */
    private void onChunkCreation(@NotNull ChunkBase chunk) {
        ChunkInfo info = getChunkInfo(chunk);
        BitSet savedFragments = info.getSavedFragments();
        //TODO: IMPLEMENT SAVING LOGIC
    }

    @NotNull
    private ChunkInfo getChunkInfo(@NotNull ChunkBase chunk) {
        File chunkDirectory = getChunkDirectory(chunk);
        if (!chunkDirectory.exists())
            throw new RuntimeException(new FileNotFoundException("Chunk file " + chunkDirectory.getPath() + " not found, after requested to restored it's saved state."));
        File chunkInfoFile = getChunkInfoFile(chunkDirectory);
        if (!chunkInfoFile.exists())
            throw new RuntimeException(new FileNotFoundException("Chunk info file " + chunkInfoFile.getPath() + " not found, after requested to restored it's saved state."));
        try {
            return this.gmc.fromGMC(new BufferedInputStream(new FileInputStream(chunkInfoFile)), ChunkInfo.class);
        } catch (IOException e) {
            throw new RuntimeException("ChunkInfo file magically disappeared", e);
        }
    }

    @NotNull
    private File getChunkInfoFile(@NotNull File chunkDirectory) {
        return new File(chunkDirectory, "chunk_info.json");
    }

    @NotNull
    private File getChunkDataFile(@NotNull File chunkDirectory) {
        return new File(chunkDirectory, "block_storage.bsa");
    }

    @NotNull
    private File getChunkDirectory(@NotNull ChunkBase chunk) {
        return new File(this.chunksDirectory, "chunk_" + chunk.getX() + "_" + chunk.getZ());
    }

    /**
     * @param world the world instance
     * @param chunk the given chunk
     * @return true if terrain generation should be invoked for the given chunk
     */
    private boolean shouldInvokedTerrainGeneration(@NotNull WorldBase world, @NotNull ChunkBase chunk) {
        return !world.getWorldChunkHandler().chunkExistsAtOrigin(chunk.getChunkOrigin());
    }

    public static class WorldOptions {

        @NotNull
        @SerializedProperty("spawn_point")
        private final Vector3 spawnPoint;

        @SerializedProperty("world_height")
        private final int height;

        public WorldOptions(@NotNull Vector3 spawnPoint, int height) {
            this.spawnPoint = spawnPoint;
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        @NotNull
        public Vector3 getSpawnPoint() {
            return spawnPoint;
        }
    }

    public static class ChunkInfo {

        @NotNull
        @SerializedProperty("chunk_origin")
        private final Vec2i origin;

        @SerializedProperty("palette_version")
        private final int paletteVersion;

        @NotNull
        @SerializedProperty("saved_fragments")
        private final BitSet savedFragments;

        public ChunkInfo(@NotNull Vec2i origin, int paletteVersion, @NotNull BitSet savedFragments) {
            this.origin = origin;
            this.paletteVersion = paletteVersion;
            this.savedFragments = savedFragments;
        }

        @NotNull
        public Vec2i getOrigin() {
            return origin;
        }

        public int getPaletteVersion() {
            return paletteVersion;
        }

        @NotNull
        public BitSet getSavedFragments() {
            return savedFragments;
        }
    }
}
