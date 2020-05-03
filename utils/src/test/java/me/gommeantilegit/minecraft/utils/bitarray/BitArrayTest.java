package me.gommeantilegit.minecraft.utils.bitarray;

import io.netty.buffer.ByteBuf;
import org.junit.Test;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.*;

public class BitArrayTest {

    @Test
    public void getValue() {
        BitArray bitArray = new BitArray(10, 8);
        bitArray.set(0, 14);
        assertEquals(14, bitArray.get(0));
        bitArray.delete();
    }

    @Test
    public void getValue_2() {
        BitArray bitArray = new BitArray(10, 13);
        bitArray.set(0, 14);
        bitArray.set(1, 14);
        assertEquals(14, bitArray.get(0));
        assertEquals(14, bitArray.get(1));
        bitArray.delete();
    }

    @Test
    public void getValue_2_1() {
        BitArray bitArray = new BitArray(10, 13);
        bitArray.set(0, 14);
        bitArray.set(1, 255);
        assertEquals(14, bitArray.get(0));
        assertEquals(255, bitArray.get(1));
        bitArray.delete();
    }

    @Test
    public void getValue_2_2() {
        BitArray bitArray = new BitArray(10, 13);
        bitArray.set(0, 14);
        bitArray.set(1, 8190);
        assertEquals(14, bitArray.get(0));
        assertEquals(8190, bitArray.get(1));
        bitArray.delete();
    }

    @Test
    public void getValue_max_value() {
        BitArray bitArray = new BitArray(10, 6);
        bitArray.set(0, 63);
        bitArray.set(1, 62);
        assertEquals(63, bitArray.get(0));
        assertEquals(62, bitArray.get(1));
        bitArray.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getValue_max_value_fail_too_high() {
        BitArray bitArray = new BitArray(10, 6);
        bitArray.set(0, 64);
    }

    @Test
    public void getValue_max_value_2() {
        BitArray bitArray = new BitArray(10, 6);
        bitArray.set(0, 63);
        assertEquals(63, bitArray.get(0));
    }

    @Test
    public void accessLastElement() {
        BitArray bitArray = new BitArray(100, 6);
        bitArray.set(99, 31);
        assertEquals(31, bitArray.get(99));
    }

    @Test
    public void dump() {
        BitArray bitArray = new BitArray(4, 5);
        bitArray.set(0, 31);
        bitArray.set(1, 30);
        bitArray.set(2, 29);
        bitArray.set(3, 28);
        byte[] array = new byte[bitArray.getByteSize()];
        ByteBuffer buffer = ByteBuffer.wrap(array);
        bitArray.store(buffer);
        System.out.println(Arrays.toString(array));
    }
}