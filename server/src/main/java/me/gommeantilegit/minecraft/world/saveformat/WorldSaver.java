package me.gommeantilegit.minecraft.world.saveformat;

import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.nbt.impl.NBTFloat;
import me.gommeantilegit.minecraft.nbt.writer.NBTStreamWriter;
import me.gommeantilegit.minecraft.packet.packets.server.ServerChunkDataPacket;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WorldSaver {

    /**
     * ServerWorld to be saved
     */
    @NotNull
    private final ServerWorld world;

    /**
     * @param world sets {@link #world}
     */
    public WorldSaver(@NotNull ServerWorld world) {
        this.world = world;
    }

    /**
     * Writes the data representing {@link #world} to the output stream
     *
     * @param outputStream the zip output stream
     */
    public void save(@NotNull ZipOutputStream outputStream) throws IOException {
        Future<Void> future = this.world.pauseTick();
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to pause world tick", e);
        }
        outputStream.putNextEntry(new ZipEntry("worldGeneratorOptions"));
        // Saving world generation options
        {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(byteOut);
            NBTArray array = WorldGenerator.NBT_CONVERTER.toNBTData(this.world.getWorldGenerator());
            NBTStreamWriter nbtStreamWriter = new NBTStreamWriter(dataOut);
            nbtStreamWriter.write(array);
            outputStream.write(byteOut.toByteArray());
            outputStream.closeEntry();
        }
        outputStream.closeEntry();
        outputStream.putNextEntry(new ZipEntry("worldData"));

        // Saving world data
        {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(byteOut);
            NBTStreamWriter nbtStreamWriter = new NBTStreamWriter(dataOut);
            nbtStreamWriter.write(new NBTArray(new NBTObject[]{
                    // Spawn point
                    new NBTFloat(world.getSpawnPoint().x),
                    new NBTFloat(world.getSpawnPoint().y),
                    new NBTFloat(world.getSpawnPoint().z)

            }));
            outputStream.write(byteOut.toByteArray());
            outputStream.closeEntry();
        }

        ServerChunkDataPacket.Encoder packetEncoder = new ServerChunkDataPacket.Encoder();

//        synchronized (world.getWorldChunkHandler().getChunks()) {

        this.world.getWorldChunkHandler().collectChunks().forEach(c -> {
            try {
                outputStream.putNextEntry(new ZipEntry("chunk_" + c.getX() + "_" + c.getZ()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ServerChunkDataPacket packet = new ServerChunkDataPacket(null, c.getChunkOrigin().asLibGDXVec2D(), c);
            BitByteBuffer tempBuffer = new BitByteBuffer();
            tempBuffer.useBytes();
            packetEncoder.serialize(
                    packet, tempBuffer
            );
            try {
                outputStream.write(tempBuffer.retrieveBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                outputStream.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
//        }
        outputStream.close();
        this.world.resumeTick();
    }


    @NotNull
    public ServerWorld getWorld() {
        return world;
    }
}
