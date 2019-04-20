package me.gommeantilegit.minecraft.utils.serialization.buffer;

import org.junit.Test;

import static org.junit.Assert.*;

public class BitByteBufferTest {

    @Test
    public void writeBit() {
        BitByteBuffer bitByteBuffer = new BitByteBuffer();

        long start = System.nanoTime();

        bitByteBuffer.useBytes();
        bitByteBuffer.useBits();

        bitByteBuffer.writeByte((byte) 126);
        assertEquals(126, bitByteBuffer.readByte());

        bitByteBuffer.writeInt(3451672);
        assertEquals(3451672, bitByteBuffer.readInt());

        bitByteBuffer.writeFloat(3.1415f);
        assertEquals(3.1415f, bitByteBuffer.readFloat(), 0f);

        bitByteBuffer.writeLong(213847612384L);
        assertEquals(213847612384L, bitByteBuffer.readLong());

        bitByteBuffer.writeShort((short) 2356);
        assertEquals((short) 2356, bitByteBuffer.readShort());

        bitByteBuffer.writeShort((short) -2356);
        assertEquals((short) -2356, bitByteBuffer.readShort());

        bitByteBuffer.writeUnsignedByte((short) 255);
        assertEquals((short) 255, bitByteBuffer.readUnsignedByte());

        bitByteBuffer.writeUnsignedShort(65534);
        assertEquals(65534, bitByteBuffer.readUnsignedShort());

        bitByteBuffer.useBits();

        bitByteBuffer.writeIntegerValue(3, 2);
        assertEquals(3, bitByteBuffer.readIntegerValue(2));

        bitByteBuffer.useBytes();
        bitByteBuffer.writeDouble(3.14);
        assertEquals(3.14, bitByteBuffer.readDouble(), 0);

        System.out.println("Took: " + (System.nanoTime() - start) + " ns");
    }
}