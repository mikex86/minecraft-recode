package me.gommeantilegit.minecraft.nbt.reader;

import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.exception.NBTCorruptionException;
import me.gommeantilegit.minecraft.nbt.impl.*;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public abstract class NBTReader<T extends NBTObject<?>> implements INBTReader<T> {

    /**
     * Identifier indicating that the following object is of a given type.
     * eg. Boolean if 1.
     */
    protected final byte id;

    public static final NBTReader<NBTByte> BYTE_READER = new NBTReader<NBTByte>((byte) 0) {

        @Override
        public NBTByte read(@NotNull DataInputStream dataInputStream) throws IOException {
            return new NBTByte(dataInputStream.readByte());
        }

        @Override
        public void write(@NotNull DataOutputStream dataOutputStream, @NotNull NBTByte object) throws IOException {
            dataOutputStream.writeByte(id);
            dataOutputStream.writeByte(object.getValue());
        }

    };

    public static final NBTReader<NBTBoolean> BOOLEAN_READER = new NBTReader<NBTBoolean>((byte) 1) {

        @Override
        public NBTBoolean read(@NotNull DataInputStream dataInputStream) throws IOException {
            return new NBTBoolean(dataInputStream.readBoolean());
        }

        @Override
        public void write(@NotNull DataOutputStream dataOutputStream, @NotNull NBTBoolean object) throws IOException {
            dataOutputStream.writeByte(id);
            dataOutputStream.writeBoolean(object.getValue());
        }

    };

    public static final NBTReader<NBTInteger> INTEGER_READER = new NBTReader<NBTInteger>((byte) 2) {

        @Override
        public NBTInteger read(@NotNull DataInputStream dataInputStream) throws IOException {
            return new NBTInteger(dataInputStream.readInt());
        }

        @Override
        public void write(@NotNull DataOutputStream dataOutputStream, @NotNull NBTInteger object) throws IOException {
            dataOutputStream.writeByte(id);
            dataOutputStream.writeInt(object.getValue());
        }
    };

    public static final NBTReader<NBTDouble> DOUBLE_READER = new NBTReader<NBTDouble>((byte) 3) {

        @Override
        public NBTDouble read(@NotNull DataInputStream dataInputStream) throws IOException {
            return new NBTDouble(dataInputStream.readDouble());
        }

        @Override
        public void write(@NotNull DataOutputStream dataOutputStream, @NotNull NBTDouble object) throws IOException {
            dataOutputStream.writeByte(id);
            dataOutputStream.writeDouble(object.getValue());
        }

    };

    public static final NBTReader<NBTFloat> FLOAT_READER = new NBTReader<NBTFloat>((byte) 4) {

        @Override
        public NBTFloat read(@NotNull DataInputStream dataInputStream) throws IOException {
            return new NBTFloat(dataInputStream.readFloat());
        }

        @Override
        public void write(@NotNull DataOutputStream dataOutputStream, @NotNull NBTFloat object) throws IOException {
            dataOutputStream.writeByte(id);
            dataOutputStream.writeFloat(object.getValue());
        }

    };

    public static final NBTReader<NBTLong> LONG_READER = new NBTReader<NBTLong>((byte) 5) {

        @Override
        public NBTLong read(@NotNull DataInputStream dataInputStream) throws IOException {
            return new NBTLong(dataInputStream.readLong());
        }

        @Override
        public void write(@NotNull DataOutputStream dataOutputStream, @NotNull NBTLong object) throws IOException {
            dataOutputStream.writeByte(id);
            dataOutputStream.writeLong(object.getValue());
        }

    };

    public static final NBTReader<NBTString> STRING_READER = new NBTReader<NBTString>((byte) 6) {

        @Override
        public NBTString read(@NotNull DataInputStream dataInputStream) throws IOException, NBTCorruptionException {
            int length = dataInputStream.readInt();
            char[] chars = new char[length];
            for (int i = 0; i < length; i++) {
                char character = dataInputStream.readChar();
                chars[i] = character;
            }

            byte endByte = dataInputStream.readByte();

            if (endByte != -1) {
                throw new NBTCorruptionException("Cannot read String!");
            }

            return new NBTString(new String(chars));
        }

        @Override
        public void write(@NotNull DataOutputStream dataOutputStream, @NotNull NBTString object) throws IOException {
            dataOutputStream.writeByte(id);
            String value = object.getValue();
            int length = value.length();
            char[] chars = value.toCharArray();
            dataOutputStream.writeInt(length);
            for (char character : chars) {
                dataOutputStream.writeChar(character);
            }
            dataOutputStream.writeByte(-1);
        }

    };

    public static final NBTReader<NBTStringMap> STRING_MAP_READER = new NBTReader<NBTStringMap>((byte) 7) {

        @Override
        public NBTStringMap read(@NotNull DataInputStream dataInputStream) throws IOException, NBTCorruptionException {
            byte idFromNBTArray = dataInputStream.readByte();
            if (idFromNBTArray != NBTARRAY_READER.id)
                throw new NBTCorruptionException("Invalidly formatted StringMap");

            NBTArray dualArray = NBTARRAY_READER.read(dataInputStream);
            return NBTStringMap.fromDualArray(dualArray);
        }

        @Override
        public void write(@NotNull DataOutputStream dataOutputStream, @NotNull NBTStringMap object) throws IOException {
            dataOutputStream.writeByte(id);
            NBTArray array = object.toNBTArray();
            NBTARRAY_READER.write(dataOutputStream, array);
        }

    };

    public static final NBTReader<NBTArray> NBTARRAY_READER = new NBTReader<NBTArray>((byte) 8) {

        @Override
        public NBTArray read(@NotNull DataInputStream dataInputStream) throws IOException, NBTCorruptionException {
            ArrayList<NBTObject<?>> objects = new ArrayList<>();
            int length = dataInputStream.readInt();
            int readElements = 0;
            while (readElements < length) {
                try {
                    byte id = dataInputStream.readByte();
                    Optional<NBTReader<?>> optional = Arrays.stream(READERS).filter(r -> r.id == id).findFirst();
                    if (optional.isPresent()) {
                        NBTReader<?> nbtReader = optional.get();
                        objects.add(nbtReader.read(dataInputStream));
                    } else {
                        throw new NBTCorruptionException("Unknown Data structure id: " + id);
                    }
                } catch (IOException e) {
                    break;
                }
                readElements++;
            }
            byte endByte = dataInputStream.readByte();
            if (endByte != -2) {
                throw new NBTCorruptionException("Failed to read NBTArray!");
            }
            return new NBTArray(objects.toArray(new NBTObject[0]));
        }

        @Override
        public void write(@NotNull DataOutputStream dataOutputStream, @NotNull NBTArray object) throws IOException {
            dataOutputStream.writeByte(id);
            NBTObject<?>[] objects = object.getValue();
            dataOutputStream.writeInt(objects.length);
            for (NBTObject<?> nbtObject : objects) {
                if (nbtObject instanceof NBTInteger) {
                    INTEGER_READER.write(dataOutputStream, (NBTInteger) nbtObject);
                } else if (nbtObject instanceof NBTDouble) {
                    DOUBLE_READER.write(dataOutputStream, (NBTDouble) nbtObject);
                } else if (nbtObject instanceof NBTByte) {
                    BYTE_READER.write(dataOutputStream, (NBTByte) nbtObject);
                } else if (nbtObject instanceof NBTString) {
                    STRING_READER.write(dataOutputStream, (NBTString) nbtObject);
                } else if (nbtObject instanceof NBTLong) {
                    LONG_READER.write(dataOutputStream, (NBTLong) nbtObject);
                } else if (nbtObject instanceof NBTFloat) {
                    FLOAT_READER.write(dataOutputStream, (NBTFloat) nbtObject);
                } else if (nbtObject instanceof NBTStringMap) {
                    STRING_MAP_READER.write(dataOutputStream, (NBTStringMap) nbtObject);
                } else if (nbtObject instanceof NBTArray) {
                    NBTARRAY_READER.write(dataOutputStream, (NBTArray) nbtObject);
                } else if (nbtObject instanceof NBTBoolean) {
                    BOOLEAN_READER.write(dataOutputStream, (NBTBoolean) nbtObject);
                }
            }
            dataOutputStream.writeByte(-2);
        }
    };

    public static final NBTReader<?>[] READERS = new NBTReader[]{
            BYTE_READER,
            BOOLEAN_READER,
            INTEGER_READER,
            DOUBLE_READER,
            FLOAT_READER,
            LONG_READER,
            STRING_READER,
            STRING_MAP_READER,
            NBTARRAY_READER
    };

    NBTReader(byte id) {
        this.id = id;
    }

    public static NBTReader<?> getReader(long id) {
        return Arrays.stream(READERS).filter(r -> r.id == id).findFirst().orElseThrow(() -> new RuntimeException("Invalid DataType id: " + id));
    }
}
