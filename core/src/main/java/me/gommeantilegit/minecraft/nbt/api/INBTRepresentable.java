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
public interface INBTRepresentable<N extends NBTObject<?>, C> {

    @NotNull
    N toNBTData(C object);

    @Nullable
    C fromNBTData(N object) throws NBTParsingException;

}
