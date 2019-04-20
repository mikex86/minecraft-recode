package me.gommeantilegit.minecraft.utils.cache;

import me.gommeantilegit.minecraft.utils.cache.request.result.CacheValueRequestResult;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class FileCachedCacheTest {

    @Test
    public void allocateElement() throws IOException {
        long start = System.currentTimeMillis();
        FileCachedCache<int[]> fileCachedCache = new FileCachedCache<>();
        fileCachedCache.allocateElement();
        long end = System.currentTimeMillis();
        System.out.println("allocateElement: " + (end - start));
        fileCachedCache.closeCash();
    }

    @Test
    public void deallocateElement() throws IOException {
        int data[] = {10, 234, 6, 2, 4, 2, 6, 21, 213};
        long start = System.currentTimeMillis();
        FileCachedCache<int[]> fileCachedCache = new FileCachedCache<>();

        int id = fileCachedCache.allocateElement();

        fileCachedCache.putData(data, id);
        fileCachedCache.deallocateElement(id);

        CacheValueRequestResult<int[]> element = fileCachedCache.getElement(id);
        assertFalse(element.isValueFound());
        assertFalse(element.isValuePresent());
        assertNull(element.getValue());

        long end = System.currentTimeMillis();
        System.out.println("deallocateElement: " + (end - start));
        fileCachedCache.closeCash();
    }

    @Test
    public void putData() throws IOException {
        int data[] = {10, 234, 6, 2, 4, 2, 6, 21, 213};
        long start = System.currentTimeMillis();
        FileCachedCache<int[]> fileCachedCache = new FileCachedCache<>();
        int id = fileCachedCache.allocateElement();
        fileCachedCache.putData(data, id);
        assertArrayEquals(fileCachedCache.getElement(id).getValue(), data);
        long end = System.currentTimeMillis();
        System.out.println("putData: " + (end - start));
        fileCachedCache.closeCash();
    }

    @Test
    public void getElement() throws IOException {
        int data[] = {10, 234, 6, 2, 4, 2, 6, 21, 213};
        long start = System.currentTimeMillis();
        FileCachedCache<int[]> fileCachedCache = new FileCachedCache<>();
        int id = fileCachedCache.allocateElement();
        fileCachedCache.putData(data, id);
        CacheValueRequestResult<int[]> element = fileCachedCache.getElement(id);

        assertTrue(element.isValuePresent());
        assertTrue(element.isValueFound());
        assertNotNull(element.getValue());
        assertArrayEquals(data, element.getValue());

        long end = System.currentTimeMillis();
        System.out.println("getElement: " + (end - start));
        fileCachedCache.closeCash();
    }

    @Test
    public void optimizeElement() throws IOException {
        int data[] = {10, 234, 6, 2, 4, 2, 6, 21, 213};
        long start = System.currentTimeMillis();
        FileCachedCache<int[]> fileCachedCache = new FileCachedCache<>();
        int id = fileCachedCache.allocateElement();
        fileCachedCache.putData(data, id);
        {
            long accessStart = System.nanoTime();
            CacheValueRequestResult<int[]> element = fileCachedCache.getElement(id);
            System.out.println("Memory access: " + (System.nanoTime() - accessStart) + "ns");
            assertTrue(element.isValuePresent());
            assertTrue(element.isValueFound());
            assertArrayEquals(data, element.getValue());
        }
        {
            fileCachedCache.optimizeElement(id);
            long accessStart = System.nanoTime();
            CacheValueRequestResult<int[]> element = fileCachedCache.getElement(id);
            System.out.println("RAS access: " + (System.nanoTime() - accessStart) + "ns");
            assertTrue(element.isValuePresent());
            assertTrue(element.isValueFound());
            assertNotNull(element.getValue());
            assertArrayEquals(data, element.getValue());
        }
        {
            fileCachedCache.deallocateElement(id);
            CacheValueRequestResult<int[]> element = fileCachedCache.getElement(id);
            assertFalse(element.isValuePresent());
            assertFalse(element.isValueFound());
            assertNull(element.getValue());
        }
        long end = System.currentTimeMillis();
        System.out.println("getElement: " + (end - start));
        fileCachedCache.closeCash();
    }
}