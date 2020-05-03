package me.gommeantilegit.minecraft.utils.memory;

import org.junit.Test;

import static org.junit.Assert.*;

public class NativeMemoryTest {

    @Test
    public void get_clear() {
        NativeMemory memory = new NativeMemory(100, true);
        for (int i = 0; i < memory.getSize(); i++) {
            assertEquals(0, memory.getByte(i));
        }
        memory.free();
    }

    @Test
    public void set() {
        NativeMemory memory = new NativeMemory(100, false);
        memory.setByte(0, (byte) 23);
        memory.setByte(1, (byte) 45);
        assertEquals(memory.getByte(1), (byte) 45);
        assertEquals(memory.getByte(0), (byte) 23);
        memory.free();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void set_negative_index_fail() {
        NativeMemory memory = new NativeMemory(100, false);
        memory.setByte(-1, (byte) 23);
        memory.free();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void set_size_index_fail() {
        NativeMemory memory = new NativeMemory(100, false);
        memory.setByte(100, (byte) 23);
        memory.free();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void set_out_of_bounds_index_fail() {
        NativeMemory memory = new NativeMemory(100, false);
        memory.setByte(101, (byte) 23);
        memory.free();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void set_too_large_index_fail() {
        NativeMemory memory = new NativeMemory(100, false);
        memory.setByte(287932342, (byte) 23);
        memory.free();
    }

    @Test(expected = IllegalStateException.class)
    public void free_set_fail() {
        NativeMemory memory = new NativeMemory(100, false);
        memory.free();
        memory.setByte(0, (byte) 12);
    }

    @Test
    public void getSize() {
        NativeMemory memory = new NativeMemory(100, false);
        assertEquals(100, memory.getSize());
    }
}