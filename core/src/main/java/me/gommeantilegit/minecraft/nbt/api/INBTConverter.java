package me.gommeantilegit.minecraft.nbt.api;

import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.exception.NBTParsingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents and object that can be represented as NBT Data
 * @param <N> The type of NBTObject that should represent the object
 * @param <C> The type of the object to be represented.
 */
public interface INBTConverter<N extends NBTObject<?>, C> {

    @NotNull
    N toNBTData(C object);

    /**
     * @param object the NBTObject to be parsed into an instance of type C
     * @param args extra arguments if needed
     * @param <T> type of the arguments
     * @return the object instance represented by object
     * @throws NBTParsingException if parsing fails
     */
    <T> C fromNBTData(N object, T... args) throws NBTParsingException;

}
