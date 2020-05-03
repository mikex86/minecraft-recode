package me.gommeantilegit.minecraft.world.saveformat;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.nbt.NBTStreamReader;
import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.nbt.impl.NBTFloat;
import me.gommeantilegit.minecraft.packet.exception.PacketDecodingException;
import me.gommeantilegit.minecraft.packet.packets.server.ServerChunkDataPacket;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.utils.collections.LongHashMap;
import me.gommeantilegit.minecraft.utils.io.IOUtils;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static me.gommeantilegit.minecraft.utils.io.IOUtils.decompress;

@SideOnly(side = Side.SERVER)
public class WorldLoader {

    @NotNull
    private final ServerMinecraft mc;

    /**
     * NBT array representing the {@link WorldGenerator} instance of the world
     */
    @NotNull
    private final NBTArray worldGeneratorOptions;

    /**
     * Stores a hash of the chunk origin and the parent chunk data packet
     */
    @NotNull
    private final LongHashMap<ServerChunkDataPacket> chunkData = new LongHashMap<>();

    /**
     * NBT array storing world meta data such as the spawn point
     */
    @NotNull
    private final NBTArray worldData;

    /**
     * @param worldFile the world file
     * @param mc        the Minecraft instance
     */
    public WorldLoader(@NotNull File worldFile, @NotNull ServerMinecraft mc) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(worldFile);
        ByteArrayOutputStream fileData = new ByteArrayOutputStream();
        IOUtils.io(fileInputStream, fileData);
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(fileData.toByteArray()));
        this.mc = mc;
        ZipEntry currentEntry;
        ByteArrayOutputStream worldGenerationDataBytes = null, worldDataBytes = null;
        ServerChunkDataPacket.Decoder decoder = new ServerChunkDataPacket.Decoder();
        while ((currentEntry = zipInputStream.getNextEntry()) != null) {
            if (currentEntry.getName().equals("worldData")) {
                worldDataBytes = new ByteArrayOutputStream();
                IOUtils.io(zipInputStream, worldDataBytes);
            } else if (currentEntry.getName().equals("worldGeneratorOptions")) {
                worldGenerationDataBytes = new ByteArrayOutputStream();
                IOUtils.io(zipInputStream, worldGenerationDataBytes);
            } else if (currentEntry.getName().startsWith("chunk_")) {
                String[] chunkOriginArgs = currentEntry.getName().substring(6).split("_");
                assert chunkOriginArgs.length == 2;
                int x = Integer.parseInt(chunkOriginArgs[0]);
                int z = Integer.parseInt(chunkOriginArgs[1]);
                long hash = Vec2i.hash64(x, z);
                ByteArrayOutputStream packetByteOut = new ByteArrayOutputStream();
                IOUtils.io(zipInputStream, packetByteOut);
                BitByteBuffer buf = new BitByteBuffer(packetByteOut.toByteArray(), Integer.MAX_VALUE);
                buf.useBytes();
                try {
                    ServerChunkDataPacket packet = decoder.deserialize(buf, null);
                    chunkData.put(hash, packet);
                } catch (PacketDecodingException e) {
                    e.printStackTrace();
                }
            }
        }
        assert worldGenerationDataBytes != null;
        assert worldDataBytes != null;
        this.worldData = new NBTStreamReader(new DataInputStream(new ByteArrayInputStream(worldDataBytes.toByteArray()))).readObject(NBTArray.class);
        this.worldGeneratorOptions = new NBTStreamReader(new DataInputStream(new ByteArrayInputStream(worldGenerationDataBytes.toByteArray()))).readObject(NBTArray.class);
    }

    /**
     * @return the created world instance
     */
    @NotNull
    public ServerWorld loadWorld() {
        ServerWorld world = new ServerWorld(this.mc, WorldGenerator.NBT_CONVERTER.fromNBTData(this.worldGeneratorOptions, this.mc));
        world.setSpawnPoint(new Vector3(((NBTFloat) this.worldData.getValue()[0]).getValue(), ((NBTFloat) this.worldData.getValue()[1]).getValue(), ((NBTFloat) this.worldData.getValue()[2]).getValue()));
        world.setInvokeTerrainGenerationDecider(WorldLoader.this::shouldInvokedTerrainGeneration);
        world.addOnChunkCreationListener(WorldLoader.this::applyBlockStates);
        return world;
    }

    /**
     * Restores the chunk to it's saved state
     *
     * @param chunk the given chunk
     */
    private void applyBlockStates(@NotNull ChunkBase chunk) {
        ServerChunkDataPacket packet = chunkData.get(chunk.getChunkOrigin().hash64());
        if (packet != null)
            chunk.setChunkData(decompress(packet.getChunkData()), packet.getChunkSectionsSent());
    }

    /**
     * @param world the world instance
     * @param chunk the given chunk
     * @return true if terrain generation should be invoked for the given chunk
     */
    private boolean shouldInvokedTerrainGeneration(@NotNull WorldBase world, @NotNull ChunkBase chunk) {
        return !world.getWorldChunkHandler().chunkExistsAtOrigin(chunk.getChunkOrigin()); // Asserts that the chunk gets added after terrain generation has finished
    }

}
