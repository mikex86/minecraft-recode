package me.gommeantilegit.minecraft.nbt.impl;

import me.gommeantilegit.minecraft.nbt.NBTObject;

public class NBTArray extends NBTObject<NBTObject<?>[]> {

    /**
     * @param value          the array of nbt objects
     */
    public NBTArray(NBTObject<?>[] value) {
        super(value);
    }
}
