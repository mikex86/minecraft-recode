package me.gommeantilegit.minecraft.utils.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class IOUtils {

    /**
     * Default Buffer size
     */
    private static final int BUFFER_SIZE = 4096;

    /**
     * Reads all bytes from an input stream and writes them to an output stream.
     */
    public static long io(InputStream input, OutputStream output) throws IOException {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = input.read(buf)) > 0) {
            output.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }

    /**
     * Compresses the given array
     * @param bytes the byte array to compress
     * @return the compressed version of the specified byte array
     */
    public static byte[] compress(byte[] bytes) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DeflaterOutputStream compress = new DeflaterOutputStream(out);
            compress.write(bytes);
            compress.flush();
            compress.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(150);
            return null;
        }
    }

    /**
     * Decompresses the given byte array
     * @param bytes the byte array to decompress
     * @return the decompressed version of the specified byte array
     */
    public static byte[] decompress(byte[] bytes) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InflaterOutputStream decompress = new InflaterOutputStream(out);
            decompress.write(bytes);
            decompress.flush();
            decompress.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(150);
            return null;
        }
    }
}
