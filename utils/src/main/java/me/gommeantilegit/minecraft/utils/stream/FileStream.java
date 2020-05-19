package me.gommeantilegit.minecraft.utils.stream;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Represents a streaming utility to read and write continuous data from a file. This utility is thread-safe. only one operation can be performed at a time.
 * Can only hold data of fixed size. Implement a DataStreamer to meet this need.
 *
 * @param <T> the type of data the stream holds
 */
public class FileStream<T> {

    @NotNull
    private final File file;

    @NotNull
    private final Object mutex = new Object();

    /**
     * The size of every element in bytes.
     */
    private final int elementSize;

    /**
     * Handles read/write calls to the underlying byte stream
     */
    @NotNull
    private final IDataStreamer<T> dataStreamer;

    /**
     * The number of elements written to the stream
     */
    private int size = 0;

    public FileStream(@NotNull File file, int elementSize, @NotNull IDataStreamer<T> dataStreamer) {
        this.file = file;
        this.elementSize = elementSize;
        this.dataStreamer = dataStreamer;
    }

    @NotNull
    public T read(int index) throws IOException {
        synchronized (this.mutex) {
            FileInputStream stream = new FileInputStream(this.file);
            stream.skip(index * this.elementSize);
            T data = this.dataStreamer.fromStream(stream);
            stream.close();
            return data;
        }
    }

    public void write(@NotNull T data) throws IOException {
        synchronized (this.mutex) {
            FileOutputStream stream = new FileOutputStream(this.file, true);
            this.dataStreamer.toStream(stream, data);
            stream.close();
            this.size++;
        }
    }

    public int size() {
        return size;
    }
}
