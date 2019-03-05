package me.gommeantilegit.minecraft.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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


}
