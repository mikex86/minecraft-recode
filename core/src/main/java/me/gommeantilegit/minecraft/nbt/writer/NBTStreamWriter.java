package me.gommeantilegit.minecraft.nbt.writer;

import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.impl.*;
import me.gommeantilegit.minecraft.nbt.reader.NBTReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;

public class NBTStreamWriter {

    @NotNull
    private final DataOutputStream dataOutputStream;

    public NBTStreamWriter(@NotNull DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public NBTStreamWriter write(@Nullable NBTObject<?> object) throws IOException {
        writeObject(object, dataOutputStream);
        return this;
    }

    public static void writeObject(@Nullable NBTObject<?> object, @NotNull DataOutputStream dataOutputStream) throws IOException {
        if (object == null)
            NBTReader.NULL_READER.write(dataOutputStream, null);
        if (object instanceof NBTInteger) {
            NBTReader.INTEGER_READER.write(dataOutputStream, (NBTInteger) object);
        } else if (object instanceof NBTDouble) {
            NBTReader.DOUBLE_READER.write(dataOutputStream, (NBTDouble) object);
        } else if (object instanceof NBTByte) {
            NBTReader.BYTE_READER.write(dataOutputStream, (NBTByte) object);
        } else if (object instanceof NBTString) {
            NBTReader.STRING_READER.write(dataOutputStream, (NBTString) object);
        } else if (object instanceof NBTLong) {
            NBTReader.LONG_READER.write(dataOutputStream, (NBTLong) object);
        } else if (object instanceof NBTFloat) {
            NBTReader.FLOAT_READER.write(dataOutputStream, (NBTFloat) object);
        } else if (object instanceof NBTStringMap) {
            NBTReader.STRING_MAP_READER.write(dataOutputStream, (NBTStringMap) object);
        } else if (object instanceof NBTArray) {
            NBTReader.NBT_ARRAY_READER.write(dataOutputStream, (NBTArray) object);
        } else if (object instanceof NBTBoolean) {
            NBTReader.BOOLEAN_READER.write(dataOutputStream, (NBTBoolean) object);
        }
    }

    @NotNull
    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }
}