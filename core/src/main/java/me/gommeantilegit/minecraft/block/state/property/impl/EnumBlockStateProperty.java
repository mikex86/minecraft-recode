package me.gommeantilegit.minecraft.block.state.property.impl;

import me.gommeantilegit.minecraft.block.state.property.BlockStateProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class EnumBlockStateProperty<T extends Enum> extends BlockStateProperty<T> {

    /**
     * Constructor for {@link EnumBlockStateProperty} where the specified Collection of allowed values are allowed values for the property
     * @param name the name of the property
     * @param valueClass the class of the enum
     * @param allowedValues the enum values that are allowed for the {@link BlockStateProperty}
     */
    public EnumBlockStateProperty(@NotNull String name, @NotNull Class<T> valueClass, T... allowedValues) {
        super(name, valueClass, Arrays.asList(allowedValues));
    }

    /**
     * Constructor for {@link EnumBlockStateProperty} where all values of the Enum are allowed values for the property
     * @param name the name of the property
     * @param valueClass the class of the enum type
     */
    public EnumBlockStateProperty(@NotNull String name, @NotNull Class<T> valueClass) {
        super(name, valueClass, Arrays.asList(valueClass.getEnumConstants()));
    }

    @NotNull
    @Override
    public String getName() {
        return super.getName();
    }
}
