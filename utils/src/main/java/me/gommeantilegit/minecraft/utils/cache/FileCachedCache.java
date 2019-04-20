package me.gommeantilegit.minecraft.utils.cache;

import com.badlogic.gdx.math.FloatArray;
import me.gommeantilegit.minecraft.utils.Clock;
import me.gommeantilegit.minecraft.utils.async.AsyncExecutor;
import me.gommeantilegit.minecraft.utils.async.AsyncResult;
import me.gommeantilegit.minecraft.utils.async.AsyncTask;
import me.gommeantilegit.minecraft.utils.cache.request.result.CacheValueRequestResult;
import me.gommeantilegit.minecraft.utils.cache.request.result.TimeOutCacheValueRequestResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

/**
 * Represents a cache that sometimes outsources the values out of random access memory onto a temporary file so save memory.
 *
 * @param <T> type of element to cache
 */
public class FileCachedCache<T extends Serializable> implements Cache<T> {

    @NotNull
    private final ArrayList<FileCachedElement> inMemoryCache = new ArrayList<>();

    /**
     * Current new element id
     */
    private int id;

    /**
     * Random access file instance for accessing {@link #tempFile}
     */
    @NotNull
    private final RandomAccessFile randomAccessFile;

    /**
     * The temporary file that optimized elements are outsourced into
     */
    @NotNull
    private final File tempFile;

    /**
     * Queue of RandomAccessFilePointers where an element was previously freed, and can be used later
     */
    @NotNull
    private Queue<FreedRASPointer> freedRASPointers = new LinkedList<>();

    /**
     * Incrementing RandomAccessFile pointer where the next element could be stored in the temp file
     */
    private long currentRASPosition = 0;

    /**
     * Async Executor instance for scheduling async tasks
     */
    @NotNull
    private final AsyncExecutor asyncExecutor = new AsyncExecutor(16);

    /**
     * Queue of ids for elements that can be reused because they are freed now.
     */
    @NotNull
    private final Queue<Integer> freeInMemoryCacheIds = new LinkedList<>();

    public FileCachedCache() throws IOException {
        this.tempFile = File.createTempFile("file_cache", ".ch");
        this.tempFile.deleteOnExit();
        this.randomAccessFile = new RandomAccessFile(tempFile, "rw");
    }

    @Override
    public int allocateElement() {
        FileCachedElement newElement = new FileCachedElement();
        newElement.inMemory = true;
        newElement.rafPos = 0;
        newElement.amountOfBytes = 0;
        newElement.value = null;
        if (freeInMemoryCacheIds.isEmpty()) {
            inMemoryCache.add(newElement);
            return id++;
        } else {
            int id = freeInMemoryCacheIds.remove();
            inMemoryCache.set(id, newElement);
            return id;
        }
    }

    @Override
    public void deallocateElement(int id) throws IndexOutOfBoundsException {
        FileCachedElement element = inMemoryCache.get(id);
        if (!element.inMemory) {
            element.freeRASMemory();
        }
        inMemoryCache.set(id, null);
        this.freeInMemoryCacheIds.add(id);
        this.id--;
    }

    @Override
    public void putData(T value, int id) throws IndexOutOfBoundsException {
        FileCachedElement element = inMemoryCache.get(id);
        if (element.inMemory)
            element.value = value;
        else {
            element.writeValue(value);
        }
    }

    @Override
    public CacheValueRequestResult<T> getElement(int id) {
        try {
            FileCachedElement element = inMemoryCache.get(id);
            if (element.inMemory)
                return new CacheValueRequestResult<>(element.value, true);
            else
                return new CacheValueRequestResult<>(element.readFromRas(), true);
        } catch (IndexOutOfBoundsException e) {
            return new CacheValueRequestResult<>(null, false);
        }
    }

    @Nullable
    @Override
    public TimeOutCacheValueRequestResult<T> getElement(int id, long timeout) {
        AsyncTask<CacheValueRequestResult<T>> task = () -> getElement(id);
        AsyncResult<CacheValueRequestResult<T>> submit = this.asyncExecutor.submit(task);
        Clock clock = new Clock(false);
        clock.reset();
        while (!submit.getFuture().isDone()) {
            if (clock.getTimePassed() > timeout) {
                submit.cancel();
                return new TimeOutCacheValueRequestResult<>(null, true, false);
            }
        }
        try {
            CacheValueRequestResult<T> requestResult = submit.getFuture().get();
            return new TimeOutCacheValueRequestResult<>(requestResult.getValue(), requestResult.isValueFound(), true);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void prepareForUsage(int id) {
        FileCachedElement element = inMemoryCache.get(id);
        if (!element.inMemory) {
            element.value = element.readFromRas();
            element.amountOfBytes = 0;
            element.rafPos = 0;
            element.inMemory = true;
        } // else - element is already prepared
    }

    @Override
    public void optimizeElement(int id) throws IndexOutOfBoundsException {
        FileCachedElement element = inMemoryCache.get(id);
        if (element.inMemory) {
            element.writeValue(element.value);
            element.value = null;
            element.inMemory = false;
        }
        // else -> Element is already optimized
    }

    /**
     * @return a RAS pointer position where the next element should be written into the {@link RandomAccessFile}
     */
    private long getRasPosition(long amountOfBytes) {
        while (!this.freedRASPointers.isEmpty()) {
            FreedRASPointer pointer = this.freedRASPointers.poll();
            if (pointer.freedBytes >= amountOfBytes) {
                this.freedRASPointers.remove(pointer);
                if (pointer.freedBytes > amountOfBytes) {
                    long startPointer = pointer.pointer + amountOfBytes;
                    free(startPointer, pointer.freedBytes - amountOfBytes);
                }
                return pointer.pointer;
            }
        }
        long pos = currentRASPosition;
        currentRASPosition += amountOfBytes;
        return pos;
    }

    /**
     * De-allocates the memory of the random access file to be used by new data
     *
     * @param pointer memory start
     * @param bytes   memory end
     */
    private void free(long pointer, long bytes) {
        this.freedRASPointers.add(new FreedRASPointer(pointer, bytes));
        for (FreedRASPointer p1 : this.freedRASPointers) {
            for (FreedRASPointer p2 : this.freedRASPointers) {
                if (p1.pointer + p1.freedBytes == p2.pointer) {
                    this.freedRASPointers.remove(p2);
                    p1.freedBytes += p2.freedBytes;
                }
            }
        }
        for (FreedRASPointer p : this.freedRASPointers) {
            if (p.pointer + p.freedBytes == this.currentRASPosition) {
                this.freedRASPointers.remove(p);
                this.currentRASPosition = p.pointer;
            }
        }
    }

    /**
     * Closes the random access file and deletes the temporary file
     *
     * @throws IOException if IO operations fail
     */
    public void closeCash() throws IOException {
        this.randomAccessFile.close();
        this.tempFile.delete();
    }

    @Override
    protected void finalize() throws Throwable {
        closeCash();
        super.finalize();
    }

    @NotNull
    public ArrayList<FileCachedElement> getInMemoryCache() {
        return inMemoryCache;
    }

    public final class FileCachedElement {

        /**
         * The value storage that stores the value of the cached element, if it is in memory
         */
        private T value = null;

        /**
         * State if the element is currently in memory. false, if it is cached in the temp file
         */
        private boolean inMemory = false;

        /**
         * The position where the element starts in the temp file, if it is not in memory. If it is in memory, this should be zero.
         */
        private long rafPos = 0;

        /**
         * The length of bytes the element needs in the random access file, if it is not in memory. If it is in memory, this should be zero.
         */
        private int amountOfBytes = 0;

        @NotNull
        public T readFromRas() {
            try {
                byte[] byteArray = new byte[amountOfBytes];
                randomAccessFile.seek(rafPos);
                randomAccessFile.read(byteArray);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
                ObjectInputStream in = new ObjectInputStream(byteArrayInputStream);
                Object value = in.readObject();
                return (T) value;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public void writeValue(@Nullable T value) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(value);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] bytes = out.toByteArray();
            try {
                long pos;
                if (this.inMemory) {
                    pos = getRasPosition(bytes.length);
                } else {
                    if (this.amountOfBytes >= bytes.length) {
                        pos = this.rafPos;
                    } else {
                        free(this.rafPos, this.amountOfBytes);
                        pos = getRasPosition(bytes.length);
                    }
                }
                randomAccessFile.seek(pos);
                randomAccessFile.write(bytes);
                rafPos = pos;
                amountOfBytes = bytes.length;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Frees the memory the RandomAccessFile memory the element used, if it was stored in it.
         *
         * @throws RuntimeException, if the element is not stored in the {@link RandomAccessFile}
         */
        public void freeRASMemory() {
            if (this.inMemory)
                throw new RuntimeException("Element not cached in RandomAccessFile! Thus cannot free it's RAS memory!");
            free(rafPos, amountOfBytes);
        }

        public T getValue() {
            return value;
        }

        public int getAmountOfBytes() {
            return amountOfBytes;
        }

        public long getRafPos() {
            return rafPos;
        }
    }

    private static final class FreedRASPointer {

        /**
         * Where the RAS memory was freed
         */
        private final long pointer;

        /**
         * How many bytes were freed
         */
        private long freedBytes;

        private FreedRASPointer(long pointer, long freedBytes) {
            this.pointer = pointer;
            this.freedBytes = freedBytes;
        }
    }
}
