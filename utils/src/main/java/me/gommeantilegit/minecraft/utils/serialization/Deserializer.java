package me.gommeantilegit.minecraft.utils.serialization;

import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.utils.serialization.exception.DeserializationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an object capable of de-serializing a given byte buffer into an equivalent object instance
 * @param <T> the type of object to de-serialize
 */
public interface Deserializer<T> {

    /**
     * De-serializes the contents of the specified byte buffer into an equivalent instance
     *
     * @param buffer  the buffer that the packet should be constructed from
     * @return the newly constructed packet
     */
    @Nullable
    T deserialize(@NotNull BitByteBuffer buffer) throws DeserializationException;


}
