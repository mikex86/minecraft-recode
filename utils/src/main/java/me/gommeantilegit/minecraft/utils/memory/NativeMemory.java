package me.gommeantilegit.minecraft.utils.memory;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class NativeMemory implements IMemory {

    @NotNull
    private static final Unsafe theUnsafe;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            theUnsafe = (Unsafe) f.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Failed to retrieve unsafe instance");
        }
    }

    private final int size;
    private long memory;

    /**
     * Allocates native memory.
     * ATTENTION: MEMORY IS UNCLEARED LIKE PLAIN C MALLOC!
     *
     * @param size the size to allocate
     * @see #NativeMemory(int, boolean)
     */
    public NativeMemory(int size) {
        this.size = size;
        this.memory = theUnsafe.allocateMemory(size);
    }

    /**
     * @param size  the size to allocate
     * @param clear state whether to clear the memory contents with zeros
     */
    public NativeMemory(int size, boolean clear) {
        this.size = size;
        this.memory = theUnsafe.allocateMemory(size);
        if (clear) {
            this.clear();
        }
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
        return theUnsafe.getByte(memory + index);
    }

    @Override
    public void setByte(int index, byte value) throws IndexOutOfBoundsException {
        check(index);
        theUnsafe.putByte(memory + index, value);
    }

    @Override
    public void setInt64(int index, long value) throws IndexOutOfBoundsException {
        check(index * Long.BYTES);
        theUnsafe.putLong(memory + index * Long.BYTES, value);
    }

    @Override
    public long getInt64(int index) throws IndexOutOfBoundsException {
        check(index * Long.BYTES);
        return theUnsafe.getLong(memory + index * Long.BYTES);
    }

    @Override
    public void free() {
        if (memory == -1) {
            throw new IllegalStateException("Memory already freed!");
        }
        theUnsafe.freeMemory(memory);
        memory = -1;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean isFreed() {
        return memory == -1;
    }

    @Override
    public void dump(@NotNull ByteBuffer buffer) {
        int remaining = buffer.remaining();
        if (remaining < size)
            throw new IllegalArgumentException("Supplied buffer does not fulfill the size requirements to receive the BitArray memory contents");
        for (int i = 0; i < size; i++) {
            buffer.put(getByte(i));
        }
    }

    @Override
    public void clear() {
        theUnsafe.setMemory(memory, size, (byte) 0);
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
        return "NativeMemory{" +
                "size=" + size +
                ", memory=" + memory +
                '}';
    }
}
