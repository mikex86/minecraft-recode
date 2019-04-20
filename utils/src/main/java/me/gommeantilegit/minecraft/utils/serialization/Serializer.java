package me.gommeantilegit.minecraft.utils.serialization;

import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.utils.serialization.exception.SerializationException;
import org.jetbrains.annotations.NotNull;

/**
 * An object used to serialize a given type of object as bytes
 *
 * @param <T> the type of object to serialize
 */
public interface Serializer<T> {

    /**
     * Serializes the given object into the given byte buffer
     *
     * @param object the object to be serialized
     * @param buf    the object buffer that the data should be written to
     * @throws SerializationException if serialization of the object fails
     */
    void serialize(@NotNull T object, @NotNull BitByteBuffer buf) throws SerializationException;

}
