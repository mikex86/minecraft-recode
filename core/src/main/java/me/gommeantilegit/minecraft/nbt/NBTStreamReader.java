package me.gommeantilegit.minecraft.nbt;

import me.gommeantilegit.minecraft.nbt.reader.NBTReader;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NBTStreamReader {

    @NotNull
    private final DataInputStream inputStream;

    public NBTStreamReader(@NotNull DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @NotNull
    public <T extends NBTObject<?>> T readObject(@NotNull Class<T> clazz) throws IOException {
        long id = inputStream.readByte();
        NBTObject<?> object = NBTReader.getReader(id).read(inputStream);
        return (T) object;
    }

    @NotNull
    public InputStream getInputStream() {
        return inputStream;
    }
}
