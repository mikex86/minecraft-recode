package me.gommeantilegit.minecraft.nbt.reader;

import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.exception.NBTCorruptionException;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface INBTReader<T extends NBTObject<?>> {

    /**
     * PLEASE NOTE THAT THE ID OF THE OBJECT MUST ALREADY BE READ BY THE SUPERORDINATE READER FOR THE METHOD TO WORK.
     *
     * @param dataInputStream the inputStream to read the data from
     * @return a new instance of the NBTObject typeof T according to dataInputStream
     * @throws IOException if reading of stream fails
     */
    T read(@NotNull DataInputStream dataInputStream) throws IOException, NBTCorruptionException;

    /**
     * Writes a representation of object to the given stream. Writes the id and the value of the object to the stream.
     *
     * @param object           the object to be written to the stream
     * @param dataOutputStream the stream written to
     * @throws IOException if writing to steam fails
     */
    void write(@NotNull DataOutputStream dataOutputStream, @NotNull T object) throws IOException;

}
