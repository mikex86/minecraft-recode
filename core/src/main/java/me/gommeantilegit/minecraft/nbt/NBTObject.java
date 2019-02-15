package me.gommeantilegit.minecraft.nbt;

public abstract class NBTObject<V> {

    /**
     * Value stored by the NBT object
     */
    protected V value;

    public NBTObject(V value){
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
