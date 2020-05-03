package me.gommeantilegit.minecraft.utils.memory;

import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A threadsafe reference that can be switched from weak to strong
 *
 * @param <T> the type of value to hold
 */
@ThreadSafe
public class Snap<T> {

    @Nullable
    private T value;

    @Nullable
    private WeakReference<T> weakReference = null;

    @NotNull
    private final Lock lock = new ReentrantLock();

    private Snap(@NotNull T value) {
        this.value = value;
    }

    @NotNull
    public static <T> Snap<T> of(@NotNull T value) {
        return new Snap<>(value);
    }

    /**
     * Turns the Snap into a weak reference
     */
    @NotNull
    public T makeWeak() {
        this.lock.lock();
        T value = this.value;
        if (value == null) {
            this.lock.unlock();
            throw new IllegalStateException("Snap lost it's value! Cannot make a lost reference weak!");
        }
        if (this.weakReference != null) {
            this.lock.unlock();
            throw new IllegalStateException("Snap is already a weak reference!");
        }
        this.weakReference = new WeakReference<>(this.value);
        this.value = null;
        this.lock.unlock();
        return value;
    }

    /**
     * Tries to make the reference strong again.
     *
     * @return the value if this succeeds, or nothing if the value has already been garbage collected wrapped in an optional
     */
    @NotNull
    public Optional<T> makeStrong() {
        this.lock.lock();
        if (this.value != null) {
            this.lock.unlock();
            throw new IllegalStateException("Snap is already a strong reference!");
        }
        Optional<T> value = get();
        if (!value.isPresent()) {
            this.lock.unlock();
            return Optional.empty();
        }
        this.value = value.get();
        this.weakReference = null;
        this.lock.unlock();
        return value;
    }

    /**
     * Tries to retrieve the value of the snap and returns it, if available.
     */
    @NotNull
    public Optional<T> get() {
        this.lock.lock();
        if (this.value != null) {
            this.lock.unlock();
            assert this.weakReference == null;
            return Optional.of(this.value);
        }
        assert this.weakReference != null;
        T value = this.weakReference.get();
        this.lock.unlock();
        return Optional.ofNullable(value);
    }

    @Override
    public boolean equals(Object o) {
        this.lock.lock();
        // does check for value state not reference state
        if (this == o) {
            this.lock.unlock();
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            this.lock.unlock();
            return false;
        }
        Snap<?> snap = (Snap<?>) o;
        Optional<T> op1 = get();
        Optional<?> op2 = snap.get();
        if (op1.isPresent() != op2.isPresent()) {
            this.lock.unlock();
            return false;
        } else {
            if (op1.isPresent()) {
                T o1 = op1.get();
                Object o2 = op2.get();
                this.lock.unlock();
                return o1 == o2 || o1.equals(o2);
            } else {
                this.lock.unlock();
                return true;
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(get().orElse(null));
    }

    public boolean isWeak() {
        if (isDeleted()) {
            throw new IllegalStateException("Snap deleted");
        }
        this.lock.lock();
        boolean state = this.weakReference != null;
        if (state && this.value != null) {
            this.lock.unlock();
            throw new IllegalStateException("Snap illegal state!");
        }
        this.lock.unlock();
        return state;
    }

    public boolean isStrong() {
        if (isDeleted()) {
            throw new IllegalStateException("Snap deleted");
        }
        this.lock.lock();
        boolean state = this.value != null;
        if (state && this.weakReference != null) {
            this.lock.unlock();
            throw new IllegalStateException("Snap illegal state!");
        }
        this.lock.unlock();
        return state;
    }

    public boolean isDeleted() {
        if (this.value == null && this.weakReference == null) {
            throw new IllegalStateException("Snap illegal state!");
        }
        return this.value == null && this.weakReference.get() == null;
    }
}
