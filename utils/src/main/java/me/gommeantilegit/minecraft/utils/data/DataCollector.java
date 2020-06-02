package me.gommeantilegit.minecraft.utils.data;

import me.gommeantilegit.minecraft.utils.stream.FileStream;
import me.gommeantilegit.minecraft.utils.stream.IDataStreamer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Collects data of type double. Data is stored in a temporary file.
 */
public class DataCollector<T> {

    @NotNull
    private final Thread writingThread;

    /**
     * Queue for data to collect asynchronously
     */
    @NotNull
    private final Queue<T> dataQueue = new ConcurrentLinkedQueue<>();

    @NotNull
    private final FileStream<T> stream;

    public DataCollector(@NotNull IDataStreamer<T> streamer) {
        try {
            File file = File.createTempFile("FloatDataCollector", "stream");
            file.deleteOnExit();
            this.stream = new FileStream<>(file, Float.BYTES, streamer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create FloatDataAllocator temp file!");
        }
        this.writingThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                while (!dataQueue.isEmpty()) {
                    T dataPoint = dataQueue.remove();
                    try {
                        collect(dataPoint);
                    } catch (IOException e) {
                        System.err.println("Write data point to stream: " + dataPoint);
                    }
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "FloatDataCollector-AsyncThread");
        this.writingThread.setDaemon(true);
        this.writingThread.start();
    }

    /**
     * Collects the given data-point
     *
     * @param dataPoint the data point to collect
     */
    public void collect(@NotNull T dataPoint) throws IOException {
        this.stream.write(dataPoint);
    }

    /**
     * Collects the given data-point
     *
     * @param dataPoint the data point to collect
     */
    public void collectAsync(@NotNull T dataPoint) {
        this.dataQueue.add(dataPoint);
    }

    /**
     * @param start the start index (means index = size - index - 1)
     * @param end   the end index exclusive (index = size - index)
     * @return the data slice from the specified region. Size is limited so that out of bounds indices are ignored (size cap)
     */
    @NotNull
    public List<T> getData(int start, int end) throws IOException {
        // Reversing the indices in the array means reversing the chronology
        int startIndex = this.stream.size() - end;
        int endIndex = this.stream.size() - start;

        startIndex = Math.max(startIndex, 0);
        int length = endIndex - startIndex;
        List<T> array = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            array.add(get(startIndex + i));
        }
        return array;
    }

    @NotNull
    public T get(int index) throws IOException {
        if (index < 0 || index >= this.stream.size()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds. Size: " + this.stream.size());
        }
        return this.stream.read(index);
    }

    @Override
    protected void finalize() throws Throwable {
        this.terminate();
        super.finalize();
    }

    public void terminate() throws IOException {
        this.writingThread.interrupt();
//        this.memoryMappedFile.close();
    }

}
