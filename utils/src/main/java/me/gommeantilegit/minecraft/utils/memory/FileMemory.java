package me.gommeantilegit.minecraft.utils.memory;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileMemory implements IMemory {

    private final int size;

    @NotNull
    private final File file;

    @NotNull
    private final RandomAccessFile randomAccessFile;

    @NotNull
    private final MappedByteBuffer mappedBuffer;

    /**
     * Allocates native memory. Initialized with zeros
     *
     * @param size the size to allocate
     */
    public FileMemory(int size) throws IOException {
        this.size = size;
        this.file = File.createTempFile("FileMemory", "mm");
        this.file.deleteOnExit();
        this.randomAccessFile = new RandomAccessFile(this.file, "rw");
        this.randomAccessFile.setLength(size);
        this.mappedBuffer = this.randomAccessFile.getChannel()
                .map(FileChannel.MapMode.READ_WRITE, 0, size);
    }

    private void check(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for memory size " + size);
        if (isFreed())
            throw new IllegalStateException("Memory is freed!");
    }

    @Override
    public byte getByte(int index) throws IndexOutOfBoundsException {
        check(index);
        return this.mappedBuffer.get(index);
    }

    @Override
    public void setByte(int index, byte value) throws IndexOutOfBoundsException {
        check(index);
        this.mappedBuffer.put(index, value);
    }

    @Override
    public void setInt64(int index, long value) throws IndexOutOfBoundsException {
        check(index * Long.BYTES);
        this.mappedBuffer.putLong(index * Long.BYTES, value);
    }

    @Override
    public long getInt64(int index) throws IndexOutOfBoundsException {
        check(index * Long.BYTES);
        return this.mappedBuffer.getLong(index * Long.BYTES);
    }

    @Override
    public void free() {
        if (!this.file.exists()) {
            throw new IllegalStateException("Memory already freed!");
        }
        try {
            this.randomAccessFile.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to free FileMemory. Random Access File failed to close");
        }
        this.file.delete();
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean isFreed() {
        return !this.file.exists();
    }

    @Override
    public void dump(@NotNull ByteBuffer buffer) {
        int remaining = buffer.remaining();
        if (remaining < size)
            throw new IllegalArgumentException("Supplied buffer does not fulfill the size requirements to receive the BitArray memory contents");
        this.mappedBuffer.clear();
        for (int i = 0; i < size; i++) {
            buffer.put(this.mappedBuffer);
        }
        this.mappedBuffer.clear();
    }

    @Override
    public void clear() {
        byte[] bytes = new byte[this.size];
        this.mappedBuffer.clear(); // reset pointer
        this.mappedBuffer.put(bytes); // pointer is at the end of memory
        this.mappedBuffer.clear(); // set back to zero
    }

    @Override
    public void set(@NotNull byte[] data) {
        if (data.length != size) {
            throw new IllegalStateException("Cannot set memory to contents with size different to memory size " + size);
        }
        for (int i = 0; i < size; i++) {
            setByte(i, data[i]);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!isFreed()) {
            System.err.println("NativeMemory leak detected for memory: " + this);
            free();
        }
    }

    @Override
    public String toString() {
        return "FileMemory{" +
                "size=" + size +
                ", file=" + file +
                '}';
    }
}
