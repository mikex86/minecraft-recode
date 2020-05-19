package me.gommeantilegit.minecraft.utils.stream.streamer.impl;

import me.gommeantilegit.minecraft.utils.stream.IDataStreamer;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FloatDataStreamer implements IDataStreamer<Float> {

    @NotNull
    @Override
    public Float fromStream(@NotNull InputStream stream) throws IOException {
        int ch1 = stream.read();
        int ch2 = stream.read();
        int ch3 = stream.read();
        int ch4 = stream.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        int bits = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
        return Float.intBitsToFloat(bits);
    }

    @Override
    public void toStream(@NotNull OutputStream stream, @NotNull Float data) throws IOException {
        int bits = Float.floatToIntBits(data);
        stream.write((bits >>> 24) & 0xFF);
        stream.write((bits >>> 16) & 0xFF);
        stream.write((bits >>>  8) & 0xFF);
        stream.write((bits) & 0xFF);
    }
}
