package me.gommeantilegit.minecraft.utils.collection;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class WeakBuffer<T> implements Iterable<T> {

    @NotNull
    private final List<WeakReference<T>> backedList;

    public WeakBuffer(@NotNull List<WeakReference<T>> backedList) {
        this.backedList = backedList;
    }

    public int size() {
        return backedList.size();
    }

    public boolean isEmpty() {
        return backedList.isEmpty();
    }

    public void add(T value) {
        this.backedList.add(new WeakReference<>(value));
    }

    public void remove(T value) {
        this.backedList.removeIf(tWeakReference -> tWeakReference.equals(value));
    }

    public boolean contains(Object o) {
        for (WeakReference<T> reference : backedList) {
            if (reference.get() == o)
                return true;
        }
        return false;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private boolean hasNext = true;
            private T next;

            @NotNull
            private final Iterator<WeakReference<T>> referenceIterator = backedList.iterator();

            @Override
            public boolean hasNext() {
                while (this.referenceIterator.hasNext()) {
                    WeakReference<T> next = this.referenceIterator.next();
                    T value = next.get();
                    if (value != null) {
                        this.next = value;
                        return true;
                    }
                }
                this.hasNext = false;
                return false;
            }

            @Override
            public T next() {
                if (!this.hasNext) {
                    throw new NoSuchElementException();
                }
                return this.next;
            }
        };
    }

}
