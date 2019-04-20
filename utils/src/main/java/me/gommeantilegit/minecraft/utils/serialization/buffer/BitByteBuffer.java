package me.gommeantilegit.minecraft.utils.serialization.buffer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.netty.buffer.ByteBuf;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.charset.StandardCharsets;

import static java.lang.Math.ceil;
import static java.lang.Math.pow;

/**
 * Custom bit based ByteBuffer with more functionality
 */
public class BitByteBuffer {

    /**
     * The factor that the needed capacity is multiplied with to get the new size of the buffers byte array, when lack of capacity is detected to perform the next writing operation.
     */
    private static final float GROWING_FACTOR = 1.15f;

    /**
     * The bit index where the buffer is reading
     */
    private int readingBitIndex = 0;

    /**
     * The bit index where the buffer is writing
     */
    private int writingBitIndex = 0;

    /**
     * The current byte array storing the written data
     */
    @NotNull
    private byte[] bytes;

    /**
     * Maximum capacity the buffer can grow to.
     * If the buffer would grow bigger than this value, a {@link BufferOverflowException} would be thrown.
     */
    private final int maxCapacity;

    /**
     * The current capacity in bytes
     */
    private int currentCapacity;

    /**
     * The index of the bit with the highest index that was set
     * This can be less than {@link #currentCapacity} * 8 because this byte buffer allows single bits to be written to it.
     */
    private int greatestBitIndex;

    /**
     * State if the buffer is using bits or bytes
     */
    private boolean useBytes = true;

    /**
     * @param initialCapacity initial byte buffer capacity
     * @param maxCapacity     maximum byte buffer capacity the buffer can grow up to
     */
    public BitByteBuffer(int initialCapacity, int maxCapacity) {
        this.bytes = new byte[initialCapacity];
        this.maxCapacity = maxCapacity;
        this.currentCapacity = bytes.length;
        this.greatestBitIndex = bytes.length * 8;
    }

    /**
     * @param initialArray initial byte array that sets the data of the packet buffer
     * @param maxCapacity  maximum capacity that the byte buffer can grow up to
     */
    public BitByteBuffer(@NotNull byte[] initialArray, int maxCapacity) {
        this.bytes = initialArray;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = bytes.length;
        this.greatestBitIndex = bytes.length * 8;
    }

    /**
     * Tells the buffer to use single bits instead of bytes. This may reduce performance of buffer access
     */
    public void useBits() {
        this.useBytes = false;
    }

    /**
     * Tells the buffer to use bytes instead of single bits.
     * Please note that the methods {@link #writeBit(int)} and {@link #readBit()} are unavailable until {@link #useBits()} is invoked
     */
    public void useBytes() {
        this.useBytes = true;
    }

    /**
     * Ensures that the byte buffer has the specified capacity
     *
     * @param additionalBytes the additional capacity needed in bytes
     * @throws BufferOverflowException if the byte buffer cannot grow any bigger because of the specified {@link #maxCapacity}.
     */
    public void ensureCapacity(int additionalBytes) throws BufferOverflowException {
        if (bytes.length < (greatestBitIndex / 8) + additionalBytes) {
            grow(currentCapacity + additionalBytes);
        }
    }

    /**
     * Grows the byte buffer to the specified size of bytes
     *
     * @param newCapacity the new size of the buffer in bytes.
     * @throws BufferOverflowException if the byte buffer cannot grow any bigger because of the specified {@link #maxCapacity}.
     */
    private void grow(int newCapacity) throws BufferOverflowException {
        if (newCapacity > maxCapacity) {
            throw new BufferOverflowException();
        }
        byte[] newBytes = new byte[currentCapacity = (int) (newCapacity * GROWING_FACTOR)];
        System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
        bytes = newBytes;
    }

    /**
     * Default constructor creates a packet buffer with an initial capacity of 5 that can grow up to Integer.MAX_VALUE which is 2<sup>31</sup>-1.
     */
    public BitByteBuffer() {
        this(5, Integer.MAX_VALUE);
    }

    /**
     * Creates a packet buffer copy of the specified byte buffer
     *
     * @param buf the specified byte buffer
     */
    public BitByteBuffer(@NotNull ByteBuf buf) {
        this(read(buf), buf.maxCapacity());
    }

    /**
     * @param buf a byte buffer
     * @return stores the bytes of the buffer into a byte array
     */
    @NotNull
    private static byte[] read(@NotNull ByteBuf buf) {
        int length = buf.readableBytes();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return bytes;
    }

    /**
     * Writes the given integer to the buffer
     *
     * @param integer the integer to be written to the buffer
     * @throws BufferOverflowException if the byte buffer cannot grow any bigger because of the specified {@link #maxCapacity}.
     */
    public void writeInt(int integer) {
        writeByte((byte) ((integer >>> 24)));
        writeByte((byte) ((integer >>> 16)));
        writeByte((byte) ((integer >>> 8)));
        writeByte((byte) (integer));
    }

    /**
     * Writes the given byte to the buffer
     *
     * @param value the byte to be written to the buffer
     */
    public void writeByte(byte value) {
        if (useBytes) {
            if (writingBitIndex % 8 != 0) {
                writingBitIndex = ((writingBitIndex / 8) * 8) + 8;
            }
            ensureCapacity(1);
            int byteIndex = writingBitIndex / 8;
            assert writingBitIndex % 8 == 0;
            bytes[byteIndex] = value;
            writingBitIndex += 8;
            if (writingBitIndex > greatestBitIndex)
                greatestBitIndex = writingBitIndex;
        } else {
            writeBit((value & 0b10000000) >> 7);
            writeBit((value & 0b01000000) >> 6);
            writeBit((value & 0b00100000) >> 5);
            writeBit((value & 0b00010000) >> 4);
            writeBit((value & 0b00001000) >> 3);
            writeBit((value & 0b00000100) >> 2);
            writeBit((value & 0b00000010) >> 1);
            writeBit(value & 0b00000001);
        }
    }

    /**
     * Writes the given unsigned byte to the buffer
     *
     * @param value the unsigned byte to be written to the buffer. Represented as short because... Java. value must be in interval [0;255]
     */
    public void writeUnsignedByte(short value) {
        assert value >= 0 && value <= 255;
        writeByte((byte) value);
    }

    /**
     * @return the next byte interpreted as an unsigned byte. Represented as a short because... Java. Value is in interval [0;255]
     */
    public short readUnsignedByte() {
        return (short) (readByte() & 0xFF);
    }

    /**
     * Writes the given float to the byte buffer
     *
     * @param value the float to be written to the buffer
     */
    public void writeFloat(float value) {
        writeInt(Float.floatToRawIntBits(value));
    }

    /**
     * @return the next 32 bits interpreted as a float
     */
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * @return the next byte in the byte buffer by combining the next 8 bits to a byte
     * @throws BufferUnderflowException if the buffer underflows
     */
    public byte readByte() {
        if (useBytes) {
            if (readingBitIndex % 8 != 0) {
                readingBitIndex = ((readingBitIndex / 8) * 8) + 8;
            }
            int byteIndex = readingBitIndex / 8;
            assert readingBitIndex % 8 == 0;
            readingBitIndex += 8;
            try {
                return bytes[byteIndex];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new BufferUnderflowException();
            }
        } else {
            int b1 = readBit(), b2 = readBit(), b3 = readBit(), b4 = readBit(), b5 = readBit(), b6 = readBit(), b7 = readBit(), b8 = readBit();
            return (byte) (b1 << 7 | b2 << 6 | b3 << 5 | b4 << 4 | b5 << 3 | b6 << 2 | b7 << 1 | b8);
        }
    }

    /**
     * Reads the given amount of bytes from the buffer and stores it into the array
     * @param amount the amount of bytes to read
     * @return the read byte array
     * @throws BufferUnderflowException if the buffer under-flows
     */
    public byte[] readBytes(int amount){
        byte[] bytes = new byte[amount];
        for (int i = 0; i < amount; i++) {
            bytes[i] = readByte();
        }
        return bytes;
    }

    /**
     * @return the next 32 bits of the buffer as an integer
     */
    public int readInt() {
        int ch1 = readByte() & 0xFF;
        int ch2 = readByte() & 0xFF;
        int ch3 = readByte() & 0xFF;
        int ch4 = readByte() & 0xFF;
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }

    /**
     * Writes the given block pos to the buffer
     *
     * @param blockPos the block pos to be written
     */
    public void writeBlockPos(@NotNull BlockPos blockPos) {
        this.writeInt(blockPos.getX());
        this.writeInt(blockPos.getY());
        this.writeInt(blockPos.getZ());
    }

    /**
     * Reads 3 integer values to be returned as a blockPos
     *
     * @return the block pos read
     */
    @NotNull
    public BlockPos readBlockPos() {
        return new BlockPos(readInt(), readInt(), readInt());
    }

    /**
     * Writes the specified vector to the packet buffer
     *
     * @param vec the specified vector to be written onto the packet buffer
     */
    public void writeVector3(@NotNull Vector3 vec) {
        this.writeFloat(vec.x);
        this.writeFloat(vec.y);
        this.writeFloat(vec.z);
    }

    /**
     * Writes the specified vector to the packet buffer
     *
     * @param vec the specified vector to be written onto the packet buffer
     */
    public void writeVector2(@NotNull Vector2 vec) {
        this.writeFloat(vec.x);
        this.writeFloat(vec.y);
    }

    /**
     * Reads 3 float values that are used as x,y,z components for a new vector that is returned
     *
     * @return the new vector read from the packet buffer
     */
    @NotNull
    public Vector3 readVector3() {
        return new Vector3(this.readFloat(), this.readFloat(), this.readFloat());
    }


    /**
     * Reads 2 float values that are used as x,y components for a new vector that is returned
     *
     * @return the new vector read from the packet buffer
     */
    @NotNull
    public Vector2 readVector2() {
        return new Vector2(this.readFloat(), this.readFloat());
    }


    /**
     * @return the amount of readable bytes in the buffer - it's length
     */
    public int length() {
        return this.currentCapacity;
    }

    /**
     * Writes a string the the PacketBuffer (uses UTF_8 as byte representation)
     *
     * @param string the string to be written on the buffer
     */
    public void writeString(@NotNull String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeInt(bytes.length); // Writing byte count
        writeBytes(bytes); // Writing UTF-8 bytes
    }

    /**
     * Writes all bytes of the specified byte array to the buffer
     *
     * @param bytes the bytes to be written to the buffer
     */
    public void writeBytes(@NotNull byte[] bytes) {
        for (byte b : bytes) {
            writeByte(b);
        }
    }

    /**
     * Writes the specified long to the buffer
     *
     * @param value the specified 64 bit int to be written to the buffer
     */
    public void writeLong(long value) {
        writeByte((byte) (value >>> 56));
        writeByte((byte) (value >>> 48));
        writeByte((byte) (value >>> 40));
        writeByte((byte) (value >>> 32));
        writeByte((byte) (value >>> 24));
        writeByte((byte) (value >>> 16));
        writeByte((byte) (value >>> 8));
        writeByte((byte) (value));
    }

    /**
     * @return the next 64 bits of the buffer interpreted as a long
     */
    public long readLong() {
        return (((long) readByte() << 56) +
                ((long) (readByte() & 255) << 48) +
                ((long) (readByte() & 255) << 40) +
                ((long) (readByte() & 255) << 32) +
                ((long) (readByte() & 255) << 24) +
                ((readByte() & 255) << 16) +
                ((readByte() & 255) << 8) +
                ((readByte() & 255)));

    }

    /**
     * Writes the specified double value to the buffer
     *
     * @param value the specified double value
     */
    public void writeDouble(double value) {
        writeLong(Double.doubleToLongBits(value));
    }

    /**
     * @return the next 64 bits interpreted as a double
     */
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Writes the specified short value to the byte buffer
     *
     * @param value the short value to be written to the buffer
     */
    public void writeShort(short value) {
        writeByte((byte) (value >>> 8));
        writeByte((byte) value);
    }

    /**
     * @return the next 2 bytes
     */
    public short readShort() {
        int ch1 = readByte() & 0xFF;
        int ch2 = readByte() & 0xFF;
        return (short) ((ch1 << 8) + (ch2));
    }

    /**
     * Writes the given unsigned short value to the buffer
     *
     * @param value the given unsigned short value. Represented by an integer because... Java. Value must be in interval [0;65536]
     */
    public void writeUnsignedShort(int value) {
        writeShort((short) value);
    }

    /**
     * @return the next 2 byte interpreted as an unsigned short. Represented by an integer because... Java. Value is in interval [0;65536]
     */
    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    /**
     * Reads a string from the byte buffer and returns it
     *
     * @return the read string
     */
    @NotNull
    public String readString() {
        int byteCount = readInt();
        byte[] bytes = new byte[byteCount];
        for (int i = 0; i < byteCount; i++)
            bytes[i] = readByte();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Writes the given bit to the byte-buffer
     *
     * @param bit the bit to writeByte. This value must be either 1 or 0
     * @throws BufferOverflowException if the byte buffer cannot grow any bigger because of the specified {@link #maxCapacity}.
     */
    public void writeBit(int bit) {
        if (useBytes) {
            throw new IllegalStateException("writeBit method unavailable when the buffer is using bytes instead of bits!");
        }
        assert bit == 0 || bit == 1;
        if (writingBitIndex % 8 == 0) {
            ensureCapacity(1);
        }
        int byteIndex = writingBitIndex / 8;
        int bitIndex = writingBitIndex % 8;
        if (bit == 1)
            bytes[byteIndex] |= (byte) (bit << 7 >> bitIndex);
        else
            bytes[byteIndex] &= (byte) ~(bit << 7 >> bitIndex);
        writingBitIndex++;
        if (writingBitIndex > greatestBitIndex)
            greatestBitIndex = writingBitIndex;
    }

    /**
     * @return the next bit in the buffer. Returns either 0 or 1
     * @throws java.nio.BufferUnderflowException if buffer underflows
     */
    public int readBit() {
        if (useBytes)
            throw new IllegalStateException("readBit method unavailable when the BitByteBuffer is working byte based!");

        int byteIndex = readingBitIndex / 8;
        int bitIndex = readingBitIndex++ % 8;
        try {
            return (bytes[byteIndex] & (0b1 << 7 >> bitIndex)) >> (7 - bitIndex);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new BufferUnderflowException();
        }
    }


    /**
     * Writes the given integer number to the buffer by only using the specified amount of bits.
     * If the value cannot be represented by the specified amount of bits
     * Please note, that this method is unavailable, when the buffer is using bits
     *
     * @param value the value that should be written to the buffer. Value must be positive. [0; INT_MAX_VALUE]
     * @param bits  the amount of bits that should be used
     * @throws IllegalStateException    if {@link #useBytes} is true
     * @throws IllegalArgumentException if the specified integer value is negative or if it cannot be represented by the specified amount of bits
     */
    public void writeIntegerValue(int value, int bits) throws IllegalStateException {
        if (useBytes)
            throw new IllegalStateException("Cannot use writeIntegerValue when the buffer is using bytes!");
        if (value < 0)
            throw new IllegalArgumentException("Value " + value + "cannot be negative!");
        if (value > pow(2, bits))
            throw new IllegalArgumentException("Value " + value + " cannot be represented by " + bits + " bits!");
        for (int i = 0; i < bits; i++) {
            writeBit((value & (0b1 << i)) >> i);
        }
    }

    /**
     * @param bits the amount of bits to read
     * @return the next {@code bits} amount of bits interpreted as an unsigned integer value
     */
    public int readIntegerValue(int bits) {
        int value = 0;
        for (int i = 0; i < bits; i++) {
            value |= (readBit() << i);
        }
        return value;
    }

    /**
     * @return a new byte array containing all bytes from index 0 to the byte touched with the highest byte index
     */
    public byte[] retrieveBytes() {
        byte[] bytes = new byte[bytes()];
        System.arraycopy(this.bytes, 0, bytes, 0, bytes.length);
        return bytes;
    }

    /**
     * @return the amount of bytes that were touched in the buffer
     */
    public int bytes() {
        return (int) ceil(greatestBitIndex / 8f);
    }

    public boolean isUsingBytes() {
        return useBytes;
    }

    public void setWritingBitIndex(int writingBitIndex) {
        this.writingBitIndex = writingBitIndex;
    }

    public void setReadingBitIndex(int readingBitIndex) {
        this.readingBitIndex = readingBitIndex;
    }

    /**
     * Sets the {@link #writingBitIndex} to the specified value multiplied by the size of bits of a byte (8)
     *
     * @param writingByteIndex the byte index where the buffer should write next
     */
    public void setWritingByteIndex(int writingByteIndex) {
        this.writingBitIndex = writingByteIndex * 8;
    }

    /**
     * Sets the {@link #readingBitIndex} to the specified value multiplied by the size of bits of a byte (8)
     *
     * @param readingByteIndex the byte index where the buffer should read next
     */
    public void setReadingByteIndex(int readingByteIndex) {
        this.readingBitIndex = readingByteIndex * 8;
    }

    public int getReadingBitIndex() {
        return readingBitIndex;
    }

    public int getWritingBitIndex() {
        return writingBitIndex;
    }
}
