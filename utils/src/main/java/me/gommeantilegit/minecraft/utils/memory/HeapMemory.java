package me.gommeantilegit.minecraft.utils.memory;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class HeapMemory implements IMemory {

    private byte[] bytes;

    public HeapMemory(int size) {
        this.bytes = new byte[size];
    }

    @Override
    public byte getByte(int index) throws IndexOutOfBoundsException {
        return bytes[index];
    }

    @Override
    public void setByte(int index, byte value) throws IndexOutOfBoundsException {
        bytes[index] = value;
    }

    @Override
    public long getInt64(int index) throws IndexOutOfBoundsException {
        index *= Long.BYTES;
        return (((long) bytes[index + 0] << 56) +
                ((long) (bytes[index + 1] & 255) << 48) +
                ((long) (bytes[index + 2] & 255) << 40) +
                ((long) (bytes[index + 3] & 255) << 32) +
                ((long) (bytes[index + 4] & 255) << 24) +
                ((bytes[index + 5] & 255) << 16) +
                ((bytes[index + 6] & 255) << 8) +
                ((bytes[index + 7] & 255) << 0));
    }

    @Override
    public void setInt64(int index, long value) throws IndexOutOfBoundsException {
        index *= Long.BYTES;

        bytes[index + 0] = (byte) (value >>> 56);
        bytes[index + 1] = (byte) (value >>> 48);
        bytes[index + 2] = (byte) (value >>> 40);
        bytes[index + 3] = (byte) (value >>> 32);
        bytes[index + 4] = (byte) (value >>> 24);
        bytes[index + 5] = (byte) (value >>> 16);
        bytes[index + 6] = (byte) (value >>> 8);
        bytes[index + 7] = (byte) (value >>> 0);
    }

    @Override
    public int getSize() {
        return bytes.length;
    }

    @Override
    public void free() {
        this.bytes = null;
    }

    @Override
    public boolean isFreed() {
        return this.bytes == null;
    }

    @Override
    public void dump(@NotNull ByteBuffer buffer) {
        buffer.put(this.bytes);
    }

    @Override
    public void clear() {
        Arrays.fill(this.bytes, (byte) 0);
    }

    @Override
    public void set(@NotNull byte[] data) {
        System.arraycopy(data, 0, this.bytes, 0, data.length);
    }
}
