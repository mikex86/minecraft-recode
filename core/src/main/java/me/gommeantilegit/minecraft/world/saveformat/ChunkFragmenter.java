package me.gommeantilegit.minecraft.world.saveformat;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.annotations.Unsafe;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.block.state.palette.IBlockStatePalette;
import me.gommeantilegit.minecraft.block.state.palette.IndexedBlockStatePalette;
import me.gommeantilegit.minecraft.block.state.storage.BlockStateStorage;
import me.gommeantilegit.minecraft.logging.crash.CrashReport;
import me.gommeantilegit.minecraft.utils.ArrayUtils;
import me.gommeantilegit.minecraft.utils.MathHelper;
import me.gommeantilegit.minecraft.utils.bitarray.BitArray;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static me.gommeantilegit.minecraft.world.chunk.ChunkBase.CHUNK_SIZE;

/**
 * Splits chunks up into small fragments.
 */
public class ChunkFragmenter {

    /**
     * The fragment size is 16 so that a 256*32*32 chunk would have 16*2*2 fragments which is 64 resulting in exactly 64 bits being used as flags which is the minimum length of the backing long array of a java BitSet.
     * Bits well spent.
     */
    public static final int CHUNK_FRAGMENT_SIZE = 16;

    /**
     * ForkJoin for chunk ticking
     */
    @NotNull
    private final ForkJoinPool chunkFragmenterPool;

    public ChunkFragmenter(@NotNull AbstractMinecraft mc) {
        this.chunkFragmenterPool = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors() / 4,
                pool -> {
                    final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    worker.setName("ChunkFragmenterWorker-" + worker.getPoolIndex());
                    worker.setDaemon(true);
                    return worker;
                },
                (t, e) -> mc.getLogger().crash(new CrashReport("ChunkWorker " + t.getName() + " crashed with", e)),
                false
        );
        ;
    }


    @NotNull
    public BitSet fragmentChunk(@NotNull IBlockStatePalette globalPalette, @NotNull ChunkBase chunk, @NotNull OutputStream stream) throws IOException {
        BitSet serializedFragments = new BitSet();
        Lock serializedFragmentBitsetWriteLock = new ReentrantLock();
        List<ForkJoinTask<byte[]>> tasks = new ArrayList<>((CHUNK_SIZE * CHUNK_SIZE * chunk.getHeight()) / (CHUNK_FRAGMENT_SIZE * CHUNK_FRAGMENT_SIZE * CHUNK_FRAGMENT_SIZE)); // capacity of nFragments
        for (int x = 0; x < CHUNK_SIZE; x += CHUNK_FRAGMENT_SIZE) {
            for (int z = 0; z < CHUNK_SIZE; z += CHUNK_FRAGMENT_SIZE) {
                for (int y = 0; y < chunk.getHeight(); y += CHUNK_FRAGMENT_SIZE) {
                    final int finalX = x;
                    final int finalY = y;
                    final int finalZ = z;
                    ForkJoinTask<byte[]> task = this.chunkFragmenterPool.submit(() -> {
                        try {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            DataOutputStream dataOut = new DataOutputStream(bos);
                            boolean empty = isChunkRegionEmpty(chunk, finalX, finalY, finalZ, CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE);
                            if (!empty) {
                                int fragmentIndex = getFragmentBitIndex(finalX, finalY, finalZ, chunk.getHeight());

                                serializedFragmentBitsetWriteLock.lock();
                                serializedFragments.set(fragmentIndex, true); // this section is sent
                                serializedFragmentBitsetWriteLock.unlock();

                                // Create a palette of all block states that occur in the current chunk fragment region
                                IndexedBlockStatePalette fragmentLocalPalette = createPaletteFromChunkRegion(chunk, finalX, finalY, finalZ, CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE);

                                // Create a block state storage that stores the current chunk fragment with that palette
                                BlockStateStorage fragmentBlockStorage = new BlockStateStorage(CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE, fragmentLocalPalette);

                                // Copy the block states of the chunk in the region of the fragment into that block storage
                                copyFromChunkToStorage(fragmentBlockStorage, chunk, finalX, finalY, finalZ, CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE);

                                // Get the indices of the occurring block states in the global palette provided
                                // Used to map local palette indices to global indices
                                // We do this to save bits in the element size of the palette array
                                int[] indices = fragmentLocalPalette.getInstanceIndices(globalPalette);

                                // storing the global indices as a bit array which uses exactly as many bits as needed. (min of 4 bit because the BitArray does not support it)
                                BitArray paletteBitArray = new BitArray(MathHelper.iceil(fragmentLocalPalette.getNumKeys(), 2) * 2, Math.max(4, MathHelper.getNeededBits(ArrayUtils.max(indices))));

                                // Copying the global indices to the bit array
                                for (int i = 0; i < indices.length; i++) {
                                    paletteBitArray.set(i, indices[i]);
                                }

                                // Writing the global to local mapping palette bit array onto the stream
                                {
                                    // Getting the backing bytes of the bit array
                                    byte[] paletteBitArrayBytes = paletteBitArray.getData();

                                    // writing the number of bits each element of the bit array needs
                                    dataOut.writeInt(paletteBitArray.getBits());

                                    // writing the number of elements of the bit array
                                    dataOut.writeInt(paletteBitArray.getNumElements());

                                    // writing the length of the backing byte array of bit array
                                    dataOut.writeInt(paletteBitArrayBytes.length);

                                    // writing the backing bytes of the bit array onto the stream
                                    dataOut.write(paletteBitArrayBytes);
                                }

                                // Writing the block storage using the local palette onto the stream
                                {
                                    // Getting the backing bytes of the block storage palette array bit array
                                    byte[] paletteArrayBytes = fragmentBlockStorage.getPaletteData();

                                    // writing the length of the palette array bytes
                                    dataOut.writeInt(paletteArrayBytes.length);

                                    // writing the array
                                    dataOut.write(paletteArrayBytes);
                                }

                                fragmentBlockStorage.delete(); // free the block storage
                                paletteBitArray.delete(); // free the palette bit array
                            }
                            return bos.toByteArray();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    tasks.add(task);
                }
            }
        }
        for (ForkJoinTask<byte[]> future : tasks) {
            byte[] bytes = future.join();
            stream.write(bytes);
        }
        return serializedFragments;
    }


    /**
     * De-serializes the chunk fragments and stores the block states into the destination chunk
     *
     * @param dstChunk                  the chunk to write the block states to
     * @param chunkSerializationPalette the palette that the chunk used to serialize the chunk, which is game version depended and must be retrieved considering the game version the chunk was saved with
     * @param chunkData                 the byte data of the chunk to de-serialize into the block states
     * @param chunkFragmentsSerialized  the bit that state whether a given chunk fragment x was serialized in the chunk data array (-> not empty)
     */
    @Unsafe
    public void deFragmentChunk(@NotNull ChunkBase dstChunk, @NotNull IBlockStatePalette chunkSerializationPalette, @NotNull byte[] chunkData, @NotNull BitSet chunkFragmentsSerialized) throws IOException {
        DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(chunkData));
        for (int x = 0; x < CHUNK_SIZE; x += CHUNK_FRAGMENT_SIZE) {
            for (int z = 0; z < CHUNK_SIZE; z += CHUNK_FRAGMENT_SIZE) {
                for (int y = 0; y < dstChunk.getHeight(); y += CHUNK_FRAGMENT_SIZE) {
                    int fragmentIndex = getFragmentBitIndex(x, y, z, dstChunk.getHeight());
                    boolean sent = chunkFragmentsSerialized.get(fragmentIndex);
                    if (sent) {
                        IndexedBlockStatePalette fragmentLocalPalette;
                        // reading the local to global mapping palette
                        {
                            int paletteBitArrayNumBits = dataIn.readInt();
                            int paletteBitArrayNumElements = dataIn.readInt();
                            int paletteBitArrayBytesLength = dataIn.readInt();
                            byte[] paletteBitArrayBytes = new byte[paletteBitArrayBytesLength];
                            dataIn.read(paletteBitArrayBytes);

                            BitArray paletteBitArray = new BitArray(paletteBitArrayNumElements, paletteBitArrayNumBits);
                            paletteBitArray.setData(paletteBitArrayBytes);

                            // all individual block states used in the chunk fragment
                            List<IBlockState> occurringBlockStates = new ArrayList<>();

                            for (int i = 0; i < paletteBitArray.getNumElements(); i++) {
                                occurringBlockStates.add(chunkSerializationPalette.getInstance(paletteBitArray.get(i)));
                            }
                            paletteBitArray.delete(); // free the palette bit array

                            // Create the local palette
                            fragmentLocalPalette = new IndexedBlockStatePalette(occurringBlockStates);
                        }

                        BlockStateStorage fragmentBlockStorage = new BlockStateStorage(CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE, fragmentLocalPalette);

                        {
                            // Read in the number of bytes of the byte array backing the palette array of the fragment block storage
                            int paletteArrayNumBytes = dataIn.readInt();

                            byte[] paletteArrayBytes = new byte[paletteArrayNumBytes];
                            dataIn.read(paletteArrayBytes);

                            fragmentBlockStorage.apply(paletteArrayBytes);
                        }

                        copyFromStorageToChunk(dstChunk, fragmentBlockStorage, x, y, z, CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE, CHUNK_FRAGMENT_SIZE);
                        fragmentBlockStorage.delete();
                    }
                }
            }
        }
    }

    @Unsafe
    private void copyFromStorageToChunk(@NotNull ChunkBase dstChunk, @NotNull BlockStateStorage sourceStorage, int x, int y, int z, int width, int height, int depth) {
        dstChunk.supplyBlockStates(x, y, z, width, height, depth, sourceStorage);
    }

    @NotNull
    private IndexedBlockStatePalette createPaletteFromChunkRegion(@NotNull ChunkBase chunk, int x, int y, int z, int width, int height, int depth) {
        Set<IBlockState> blockStates = new HashSet<>();
        for (int xo = 0; xo < width; xo++) {
            for (int yo = 0; yo < height; yo++) {
                for (int zo = 0; zo < depth; zo++) {
                    IBlockState blockState = chunk.getRelativeBlockState(x + xo, y + yo, z + zo);
                    blockStates.add(blockState);
                }
            }
        }
        return new IndexedBlockStatePalette(blockStates);
    }

    private void copyFromChunkToStorage(@NotNull BlockStateStorage dstStorage, @NotNull ChunkBase sourceChunk, int x, int y, int z, int width, int height, int depth) {
        for (int xo = 0; xo < width; xo++) {
            for (int yo = 0; yo < height; yo++) {
                for (int zo = 0; zo < depth; zo++) {
                    IBlockState blockState = sourceChunk.getRelativeBlockState(x + xo, y + yo, z + zo);
                    dstStorage.set(xo, yo, zo, blockState);
                }
            }
        }
    }

    private boolean isChunkRegionEmpty(@NotNull ChunkBase chunk, int x, int y, int z, int width, int height, int depth) {
        for (int xo = x; xo < x + width; xo++) {
            for (int yo = y; yo < y + height; yo++) {
                for (int zo = z; zo < z + depth; zo++) {
                    IBlockState blockState = chunk.getRelativeBlockState(xo, yo, zo);
                    if (blockState != null)
                        return false;
                }
            }
        }
        return true;
    }

    private int getFragmentBitIndex(int x, int y, int z, int height) {
        x /= CHUNK_FRAGMENT_SIZE;
        y /= CHUNK_FRAGMENT_SIZE;
        z /= CHUNK_FRAGMENT_SIZE;
        int xMax = CHUNK_SIZE / CHUNK_FRAGMENT_SIZE;
        int yMax = height / CHUNK_FRAGMENT_SIZE;
        return (z * xMax * yMax) + (y * xMax) + x;
    }
}
