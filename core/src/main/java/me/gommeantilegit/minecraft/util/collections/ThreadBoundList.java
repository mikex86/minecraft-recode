package me.gommeantilegit.minecraft.util.collections;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A Thread bound ArrayList that is modifiable by the specified boundThread.
 *  {@link #get(int)} is accessible from any thread.
 * @param <T> List Generic Type
 */
public class ThreadBoundList<T> extends ArrayList<T> {
    /**
     * The Thread that the list should be bound to
     */
    @NotNull
    private final Thread boundThread;

    public ThreadBoundList(@NotNull Thread boundThread) {
        this.boundThread = boundThread;
    }

    /**
     * Checks the boundThread accessing the ArrayList
     *
     * @throws RuntimeException if the access is illegal considering the accessing Thread.
     */
    private void checkThreadAccess() {
        if (Thread.currentThread() != boundThread) {
            throw new RuntimeException(new IllegalAccessException("Cannot access ThreadBound list from Thread [" + Thread.currentThread() + "]"));
        }
    }

    @Override
    public boolean add(T t) {
        checkThreadAccess();
        return super.add(t);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        checkThreadAccess();
        return super.addAll(c);
    }

    @Override
    public void add(int index, T element) {
        checkThreadAccess();
        super.add(index, element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        checkThreadAccess();
        return super.addAll(index, c);
    }

    @NotNull
    public Thread getBoundThread() {
        return boundThread;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
//        checkThreadAccess();
        return super.iterator();
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        checkThreadAccess();
        return super.listIterator();
    }

    @Override
    public boolean contains(Object o) {
        checkThreadAccess();
        return super.contains(o);
    }

    @Override
    public int indexOf(Object o) {
        checkThreadAccess();

        return super.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        checkThreadAccess();
        return super.lastIndexOf(o);
    }

    @Override
    public Object[] toArray() {
        return super.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        checkThreadAccess();
        return super.toArray(a);
    }

    @Override
    public T get(int index) {
        return super.get(index);
    }

    @Override
    public T set(int index, T element) {
        checkThreadAccess();
        return super.set(index, element);
    }

    @Override
    public T remove(int index) {
        checkThreadAccess();
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        checkThreadAccess();
        return super.remove(o);
    }

    @Override
    public void clear() {
        checkThreadAccess();
        super.clear();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        checkThreadAccess();
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        checkThreadAccess();
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        checkThreadAccess();
        return super.retainAll(c);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        checkThreadAccess();
        return super.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        checkThreadAccess();

        return super.subList(fromIndex, toIndex);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        checkThreadAccess();
        super.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        checkThreadAccess();
        return super.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        checkThreadAccess();
        return super.removeIf(filter);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        checkThreadAccess();
        super.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        checkThreadAccess();

        super.sort(c);
    }

    @Override
    public boolean equals(Object o) {
        checkThreadAccess();
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        checkThreadAccess();
        return super.hashCode();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        checkThreadAccess();
        return super.containsAll(c);
    }

    @Override
    public String toString() {
        checkThreadAccess();
        return super.toString();
    }

    @Override
    public Stream<T> stream() {
        checkThreadAccess();
        return super.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        checkThreadAccess();
        return super.parallelStream();
    }
}
