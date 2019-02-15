package me.gommeantilegit.minecraft.nbt.impl;

import me.gommeantilegit.minecraft.nbt.NBTObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NBTStringMap extends NBTObject<NBTStringMap.MapEntrySet<String, NBTObject<?>>> implements Map<String, NBTObject<?>> {

    public NBTStringMap(MapEntrySet<String, NBTObject<?>> value) {
        super(value);
    }

    public NBTStringMap() {
        this(new MapEntrySet<>());
    }

    @NotNull
    public static NBTStringMap fromDualArray(NBTArray dualArray) {
        NBTObject<?>[] objects = dualArray.getValue();
        ArrayList<MapEntry<String, NBTObject<?>>> entries = new ArrayList<>();
        if (objects.length % 2 == 0) {
            int element = 1;
            MapEntry<String, NBTObject<?>> currentEntry = null;
            for (NBTObject<?> object : objects) {
                if (element % 2 != 0) {
                    if (object instanceof NBTString) {
                        currentEntry = new MapEntry<>(((NBTString) object).getValue(), null);
                    } else {
                        throw new RuntimeException("Object must be String!");
                    }
                } else {
                    currentEntry.setValue(object);
                    entries.add(currentEntry);
                }
                element++;
            }
        } else {
            throw new RuntimeException("Size of object array not even!");
        }
        MapEntrySet<String, NBTObject<?>> entrySet = new MapEntrySet<>();
        entrySet.getEntries().addAll(entries);
        return new NBTStringMap(entrySet);
    }

    @NotNull
    public NBTArray toNBTArray() {
        ArrayList<NBTObject<?>> objects = new ArrayList<>();
        for (MapEntry<String, NBTObject<?>> entry : value.entries) {
            objects.add(new NBTString(entry.key));
            objects.add(entry.value);
        }
        return new NBTArray(objects.toArray(new NBTObject<?>[0]));
    }

    @Override
    public int size() {
        return this.value.entries.size();
    }

    @Override
    public boolean isEmpty() {
        return value.entries.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.value.entries.stream().anyMatch(e -> Objects.equals(e.key, key));
    }

    @Override
    public boolean containsValue(Object value) {
        return this.value.entries.stream().anyMatch(e -> Objects.equals(e.value, value));
    }

    @Override
    public NBTObject<?> put(@NotNull String key, @NotNull NBTObject<?> value) {
        Optional<MapEntry<String, NBTObject<?>>> optional = this.value.entries.stream().filter(e -> Objects.equals(e.key, key)).findFirst();
        if (optional.isPresent()) {
            MapEntry<String, NBTObject<?>> entry = optional.get();
            NBTObject<?> prevValue = entry.getValue();
            entry.setValue(value);
            return prevValue;
        } else {
            this.value.entries.add(new MapEntry<>(key, value));
            return null;
        }
    }

    @Override
    public NBTObject<?> remove(Object key) {
        Optional<MapEntry<String, NBTObject<?>>> first = this.value.entries.stream().filter(e -> Objects.equals(key, e.key)).findFirst();
        if (first.isPresent()) {
            MapEntry<String, NBTObject<?>> prevValue = first.get();
            this.value.entries.remove(prevValue);
            return prevValue.getValue();
        } else {
            return null;
        }
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends NBTObject<?>> m) {
        for (Entry<? extends String, ? extends NBTObject<?>> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        value.entries.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (MapEntry<String, NBTObject<?>> entry : this.value.entries) {
            set.add(entry.key);
        }
        return set;
    }

    @NotNull
    @Override
    public Collection<NBTObject<?>> values() {
        LinkedHashSet<NBTObject<?>> set = new LinkedHashSet<>();
        for (MapEntry<String, NBTObject<?>> entry : this.value.entries) {
            set.add(entry.value);
        }
        return set;
    }

    @NotNull
    @Override
    public Set<Entry<String, NBTObject<?>>> entrySet() {
        return new LinkedHashSet<>(this.value.entries);
    }

    @Nullable
    public NBTObject<?> get(Object key) {
        Optional<MapEntry<String, NBTObject<?>>> first = this.value.entries.stream().filter(e -> Objects.equals(e.key, key)).findFirst();
        return first.<NBTObject<?>>map(stringNBTObjectMapEntry -> stringNBTObjectMapEntry.value).orElse(null);
    }

    public static class MapEntrySet<K, V> {

        private final List<MapEntry<K, V>> entries = new ArrayList<>();

        public List<MapEntry<K, V>> getEntries() {
            return entries;
        }

    }

    public static class MapEntry<K, V> implements Map.Entry<K, V> {

        @Nullable
        private K key;

        @Nullable
        private V value;

        public MapEntry(@Nullable K key, @Nullable V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }
}
