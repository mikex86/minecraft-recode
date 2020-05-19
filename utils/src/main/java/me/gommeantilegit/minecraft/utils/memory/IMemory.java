package me.gommeantilegit.minecraft.utils.memory;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Represents a blueprint for index-able memory implementations
 */
public interface IMemory {

    /**
     * @param index the index to retrieve the value for
     * @return the byte value at memory[index]
     * @throws IndexOutOfBoundsException if the index is out of memory bounds
     * @throws IllegalStateException     if {@link #free()} was invoke on the memory before
     */
    byte getByte(int index) throws IndexOutOfBoundsException;

    /**
     * @param index the index to retrieve the value for
     * @param value the value that should be placed at memory[index]
     * @throws IndexOutOfBoundsException if the index is out of memory bounds
     * @throws IllegalStateException     if {@link #free()} was invoke on the memory before
     */
    void setByte(int index, byte value) throws IndexOutOfBoundsException;

    /**
     * @param index the index to retrieve the value for. Index unit: 64 bit int (NOT BYTE)
     * @return the int64 value at memory[index]
     * @throws IndexOutOfBoundsException if the index is out of memory bounds
     * @throws IllegalStateException     if {@link #free()} was invoke on the memory before
     */
    long getInt64(int index) throws IndexOutOfBoundsException;

    /**
     * @param index the index to retrieve the value for. Index unit: 64 bit int (NOT BYTE)
     * @param value the value that should be placed at memory[index]
     * @throws IndexOutOfBoundsException if the index is out of memory bounds
     * @throws IllegalStateException     if {@link #free()} was invoke on the memory before
     */
    void setInt64(int index, long value) throws IndexOutOfBoundsException;

    /**
     * @return the size of the memory in bytes
     */
    int getSize();

    /**
     * De-allocates the memory resources, or leaves it to the gc depending on the implementation
     *
     * @throws IllegalStateException if the memory was already freed.
     */
    void free();

    /**
     * @return true if {@link #free()} was once called on the memory
     */
    boolean isFreed();

    /**
     * Dumps the memory content into the buffer
     *
     * @param buffer the buffer
     * @throws IllegalStateException if the buffer is capacity is insufficient
     */
    void dump(@NotNull ByteBuffer buffer);

    /**
     * Sets the intire memory to zeros
     */
    void clear();

    /**
     * Sets the memory contents to the specified bytes
     *
     * @param data the byte contents. length must be equal to {@link #getSize()}
     */
    void set(@NotNull byte[] data);

    /**
     * @return a copy of the memory instance
     */
    @NotNull
    default IMemory copy() {
        throw new UnsupportedOperationException();
    }
}
