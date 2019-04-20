package me.gommeantilegit.minecraft.utils.cache;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class FileCachedCacheRASAllocationTest {

    @Test
    public void testRASMemoryManagement() throws IOException {
        int data[] = {10, 234, 6, 2, 4, 2, 6, 21, 213};
        FileCachedCache<int[]> fileCachedCache = new FileCachedCache<>();

        {
            int id = fileCachedCache.allocateElement();
            fileCachedCache.putData(data, id);
            fileCachedCache.optimizeElement(id);

            int id2 = fileCachedCache.allocateElement();
            fileCachedCache.putData(data, id2);
            fileCachedCache.optimizeElement(id2);

            int id3 = fileCachedCache.allocateElement();
            fileCachedCache.putData(data, id3);
            fileCachedCache.optimizeElement(id3);

            fileCachedCache.deallocateElement(id);
            fileCachedCache.deallocateElement(id2);
            fileCachedCache.deallocateElement(id3);
        }

        {
            int id2 = fileCachedCache.allocateElement();
            assertEquals(id2, 0);
            fileCachedCache.putData(data, id2);
            fileCachedCache.optimizeElement(id2);
            FileCachedCache<int[]>.FileCachedElement fileCachedElement = fileCachedCache.getInMemoryCache().get(id2);
            assertEquals(0, fileCachedElement.getRafPos());
        }
    }

}
