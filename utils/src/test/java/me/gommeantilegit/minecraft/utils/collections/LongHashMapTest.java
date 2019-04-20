package me.gommeantilegit.minecraft.utils.collections;

import org.junit.Test;

import java.util.HashMap;

import static java.lang.StrictMath.sqrt;
import static java.lang.StrictMath.toIntExact;
import static java.lang.StrictMath.toRadians;

public class LongHashMapTest {

    @Test
    public void test() {

        // Warm-up
        {
            double[] array = new double[100];
            for (int i = 0; i < 100; i++) {
                double x = i * sqrt(i) * toRadians(toIntExact(i));
                array[i] = x;
            }
            for (int i = 0; i < array.length; i++) {
                array[i] *= sqrt(array[i] * 50);
            }
        }

        System.out.println("Starting...");
        long p1 = System.currentTimeMillis();

        for (long i = 0; i < 10000000L; i++) {
            HashMap<Long, String> map = new HashMap<>();
            map.put(1234L, "Hello!");
            map.put(125L, "Lol!");
            String hello = map.get(1234L);
            String lol = map.get(125L);
            map.put(125L, hello);
            map.put(1234L, lol);
        }

        long p2 = System.currentTimeMillis();
        System.out.println("HashMap: " + (p2 - p1) + " ms");

        for (long i = 0; i < 10000000L; i++) {
            LongHashMap<String> map = new LongHashMap<>();
            map.put(1234L, "Hello!");
            map.put(125L, "Lol!");
            String hello = map.get(1234L);
            String lol = map.get(125L);
            map.put(125L, hello);
            map.put(1234L, lol);
        }

        long p3 = System.currentTimeMillis();

        System.out.println("LongHashMap: " + (p3 - p2) + " ms");

    }

}