package me.gommeantilegit.minecraft.block.state.property.impl;

import me.gommeantilegit.minecraft.block.state.property.BlockStateProperty;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class EnumBlockStateProperty<T extends Enum<?>> extends BlockStateProperty<T> {

    /**
     * Constructor for {@link EnumBlockStateProperty} where the specified Collection of allowed values are allowed values for the property
     *
     * @param name          the name of the property
     * @param valueClass    the class of the enum
     * @param defaultValue  the default value the property should hold
     * @param allowedValues the enum values that are allowed for the {@link BlockStateProperty}
     */
    @SafeVarargs
    public EnumBlockStateProperty(@NotNull String name, @NotNull Class<T> valueClass, @NotNull T defaultValue, T... allowedValues) {
        super(name, valueClass, Arrays.asList(allowedValues), defaultValue);
    }

    /**
     * Constructor for {@link EnumBlockStateProperty} where all values of the Enum are allowed values for the property
     *
     * @param name         the name of the property
     * @param defaultValue the default value the property should hold
     * @param valueClass   the class of the enum type
     */
    public EnumBlockStateProperty(@NotNull String name, @NotNull Class<T> valueClass, @NotNull T defaultValue) {
        super(name, valueClass, Arrays.asList(valueClass.getEnumConstants()), defaultValue);
    }

    @NotNull
    @Override
    public String getName() {
        return super.getName();
    }
}
