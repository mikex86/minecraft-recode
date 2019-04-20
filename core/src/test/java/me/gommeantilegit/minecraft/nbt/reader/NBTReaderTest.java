package me.gommeantilegit.minecraft.nbt.reader;

import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.NBTStreamReader;
import me.gommeantilegit.minecraft.nbt.impl.*;
import me.gommeantilegit.minecraft.nbt.writer.NBTStreamWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Objects;

import static me.gommeantilegit.minecraft.utils.MathHelper.humanReadableByteCount;

public class NBTReaderTest {

    private int x = 10, y = 2;
    private String string = "Hello ServerWorld!";
    private String string2 = "Now, this is epic";
    private long long1 = 1337;
    private float float1 = 3.1415f;

    private DataInputStream dataInputStream;

    @Test
    @Before
    public void testWriting() throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream("test.nbt"));
        NBTStringMap map = new NBTStringMap();
        map.put("float1", new NBTFloat(float1));

        NBTObject<?>[] objects = new NBTObject[]{
                new NBTInteger(x), new NBTInteger(y), new NBTString(string),
                new NBTArray(new NBTObject[]{
                        new NBTString(string2),
                        new NBTLong(long1),
                        map
                })
        };

        NBTArray array = new NBTArray(objects);
        new NBTStreamWriter(dataOutputStream).write(array);
        System.out.println("Size: " + humanReadableByteCount(dataOutputStream.size(), true));
    }

    @Test
    public void testReading() throws IOException {
        dataInputStream = new DataInputStream(new FileInputStream("test.nbt"));
        long start = System.currentTimeMillis();
        NBTStreamReader reader = new NBTStreamReader(dataInputStream);
        NBTArray read = reader.readObject(NBTArray.class);
        NBTObject<?>[] objects = read.getValue();
        NBTInteger int1 = (NBTInteger) objects[0];
        NBTInteger int2 = (NBTInteger) objects[1];
        NBTArray array1 = (NBTArray) objects[3];
        NBTString str2 = (NBTString) array1.getValue()[0];
        NBTLong long1 = (NBTLong) array1.getValue()[1];
        NBTStringMap stringMap = (NBTStringMap) array1.getValue()[2];
        NBTFloat float1 = (NBTFloat) stringMap.get("float1");
        Assert.assertEquals("Value 1 of type int read from input stream to not match original value.", x, (int) int1.getValue());
        Assert.assertEquals("Value 2 of type int read from input stream to not match original value.", y, (int) int2.getValue());
        Assert.assertEquals("Value 3 of type string read from input stream to not match original value.", string2, str2.getValue());
        Assert.assertEquals("Value 4 of type array read from input stream to not match original value.", this.long1, (long) long1.getValue());
        Assert.assertEquals("Value 5 of type float read from input stream to not match original value.", this.float1, Objects.requireNonNull(float1).getValue(), this.float1 - Objects.requireNonNull(float1).getValue());
        long end = System.currentTimeMillis();
        System.out.println("Time passed: " + (end - start));
    }

}