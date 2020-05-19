package me.gommeantilegit.minecraft.utils.stream;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles read/write calls to a underlying byte stream
 * @param <T> the type of data the streamer can deal with
 */
public interface IDataStreamer<T> {

    /**
     * @param stream the source stream to read the data from
     * @return the read data
     */
    @NotNull
    T fromStream(@NotNull InputStream stream) throws IOException;

    /**
     * @param stream the destination stream to write the data to
     * @param data the data to write to the stream
     */
    void toStream(@NotNull OutputStream stream, @NotNull T data) throws IOException;

}
