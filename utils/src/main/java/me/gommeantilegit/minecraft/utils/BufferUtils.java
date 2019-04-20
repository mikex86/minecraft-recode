package me.gommeantilegit.minecraft.utils;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class BufferUtils {

    /**
     * Reads the byte buffer and stores all read values in the returned array
     * @param buffer the buffer to be read
     * @return the new byte array
     */
    @NotNull
    public static byte[] toArray(@NotNull ByteBuffer buffer) {
        byte[] array = new byte[buffer.remaining()];
        buffer.get(array);
        return array;
    }
}
