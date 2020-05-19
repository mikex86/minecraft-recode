package me.gommeantilegit.minecraft.utils.bitarray;

import me.gommeantilegit.minecraft.utils.MathHelper;
import me.gommeantilegit.minecraft.utils.memory.HeapMemory;
import me.gommeantilegit.minecraft.utils.memory.IMemory;
import me.gommeantilegit.minecraft.utils.memory.NativeMemory;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Represents an array elements with bit sizes not bound to be multiples of 8
 */
public class BitArray {

    /**
     * The backing memory
     */
    @NotNull
    private final IMemory memory;

    /**
     * The number of bits an element has
     */
    private final int bits;

    /**
     * The number of elements sized {@link #bits} the bit array can store
     */
    private final int numElements;

    /**
     * The maximum value that an element in the bit array can have due to {@link #bits}
     */
    private final long elementMaxValue;

    /**
     * The size of the backing memory (includes necessary padding) (duh)
     */
    private final int byteSize;

    /**
     * @param numElements the number of elements the array should be able to store. Must be positive and event
     * @param bits        the number of bits each element of the array uses
     * @throws IllegalStateException if #size is not positive or not even or if bits is less than 4
     */
    public BitArray(int numElements, int bits) {
        if (numElements <= 0 || numElements % 2 == 1) {
            throw new IllegalArgumentException("NibbleArray must be supplied with a positive, even size");
        }
        if (bits < 4) {
            throw new IllegalStateException("Elements bits must be greater or equal to 4");
        }
        this.byteSize = MathHelper.iceil(bits * numElements, Byte.SIZE);
        this.memory = new NativeMemory(byteSize, true);
        this.numElements = numElements;
        this.elementMaxValue = (1L << bits) - 1L;
        this.bits = bits;
    }

    /**
     * @param bytes the bytes to be used as the backing bytes to be interpreted as an array of bits size of #bits
     * @param bits  the number of bits each element of the array uses or if bits is less than 4
     */
    public BitArray(byte[] bytes, int bits) {
        int numElements = bytes.length / bits;
        if (numElements <= 0 || numElements % 2 == 1) {
            throw new IllegalArgumentException("NibbleArray must be supplied with a positive, even size");
        }
        if (bits < 4) {
            throw new IllegalStateException("Elements bits must be greater or equal to 4");
        }
        this.byteSize = bytes.length;
        this.memory = new NativeMemory(byteSize, false);
        this.memory.set(bytes);
        this.numElements = numElements;
        this.bits = bits;
        this.elementMaxValue = (1L << bits) - 1L;
    }

    private BitArray(@NotNull IMemory memory, int numElements, int bits) {
        if (numElements <= 0 || numElements % 2 == 1) {
            throw new IllegalArgumentException("NibbleArray must be supplied with a positive, even size");
        }
        if (bits < 4) {
            throw new IllegalStateException("Elements bits must be greater or equal to 4");
        }
        this.byteSize = MathHelper.iceil(numElements * bits, Byte.SIZE);
        this.memory = memory;
        this.numElements = numElements;
        this.bits = bits;
        this.elementMaxValue = (1L << bits) - 1L;
    }

    /**
     * Sets the value of the bit array at the specified index to the supplied value
     *
     * @param index the index
     * @param value the value (must be > 0 and <
     */
    public void set(int index, int value) {
        check(index, value);
        int bitIndex = index * this.bits;
        int startIndex = bitIndex / Long.SIZE;
        int endIndex = ((index + 1) * this.bits - 1) / Long.SIZE;
        int bitIndexCompl = bitIndex ^ (startIndex * Long.SIZE);
        this.memory.setInt64(startIndex, this.memory.getInt64(startIndex) & ~(this.elementMaxValue << bitIndexCompl) | ((long) value & this.elementMaxValue) << bitIndexCompl);
        if (startIndex != endIndex) {
            int freeBits = Long.SIZE - bitIndexCompl;
            int usedBits = this.bits - freeBits;
            this.memory.setInt64(endIndex, this.memory.getInt64(endIndex) >>> usedBits << usedBits | ((long) value & this.elementMaxValue) >> freeBits);
        }
    }

    public int get(int index) {
        check(index);
        int bitIndex = index * this.bits;
        int startIndex = bitIndex / Long.SIZE;
        int endIndex = (((index + 1) * this.bits) - 1) / Long.SIZE;
        int bitIndexCompl = bitIndex ^ (startIndex * Long.SIZE);
        if (startIndex == endIndex) {
            return (int) (this.memory.getInt64(startIndex) >>> bitIndexCompl & this.elementMaxValue);
        } else {
            int freeBits = Long.SIZE - bitIndexCompl;
            return (int) ((this.memory.getInt64(endIndex) >> bitIndexCompl | this.memory.getInt64(endIndex) << freeBits & this.elementMaxValue));
        }
    }

    /**
     * Deletes/Disposes the allocated memory
     */
    public void delete() {
        this.memory.free();
    }

    /**
     * Fills the entire bit-array with zeros
     */
    public void clear() {
        this.memory.clear();
    }

    /**
     * Stores the bit array contents into the specified buffer
     *
     * @param buffer the buffer. Size requirements must be met
     * @throws IllegalArgumentException if the buffer size is insufficient
     */
    public void store(@NotNull ByteBuffer buffer) {
        this.memory.dump(buffer);
    }

    private void check(int index) {
        if (index < 0 || index >= this.numElements) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for element count " + this.numElements);
        }
    }

    private void check(int index, int value) {
        check(index);
        if (value < 0 || value > this.elementMaxValue) {
            throw new IllegalArgumentException("Supplied value is too high for " + bits + " bits. Max value: " + this.elementMaxValue);
        }
    }

    public int getByteSize() {
        return byteSize;
    }

    /**
     * Sets the contents of the backing byte array to the specified array
     *
     * @param data the data. Length must be equal to {@link #getByteSize()}
     */
    public void setData(@NotNull byte[] data) {
        if (data.length != byteSize) {
            throw new IllegalStateException("Cannot set BitArray data to array with different size than " + byteSize);
        }
        this.memory.set(data);
    }

    /**
     * @return a copy of the bit-array contents
     */
    @NotNull
    public byte[] getData() {
        byte[] array = new byte[byteSize];
        ByteBuffer buffer = ByteBuffer.wrap(array);
        this.memory.dump(buffer);
        return array;
    }

    public int getNumElements() {
        return numElements;
    }

    public int getBits() {
        return bits;
    }

    @NotNull
    public BitArray copy() {
        return new BitArray(this.memory.copy(), this.numElements, this.bits);
    }
}
