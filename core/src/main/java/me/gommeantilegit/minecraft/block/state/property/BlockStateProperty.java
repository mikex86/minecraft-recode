package me.gommeantilegit.minecraft.block.state.property;

import me.gommeantilegit.minecraft.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a property that is parent to a {@link BlockState} instance
 * @param <T> the type of value that is parent to the property
 */
public abstract class BlockStateProperty<T> {

    /**
     * Property name
     */
    @NotNull
    private final String name;

    /**
     * Class of the value type
     */
    @NotNull
    private final Class<T> valueClass;

    /**
     * The values that the property is allowed to have
     */
    @NotNull
    private final Collection<T> allowedValues;

    public BlockStateProperty(@NotNull String name, @NotNull Class<T> valueClass, @NotNull Collection<T> allowedValues) {
        this.name = name;
        this.valueClass = valueClass;
        this.allowedValues = allowedValues;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BlockStateProperty) {
            return ((BlockStateProperty) o).name.equals(name) && ((BlockStateProperty) o).valueClass == valueClass;
        }
        return false;
    }

    /**
     * @param value the value to check
     * @return the state whether the value can be used as the value for this property meaning it must be contained in {@link #allowedValues}
     */
    public final boolean isValidValue(@NotNull T value) {
        return allowedValues.contains(value);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Class<T> getValueClass() {
        return valueClass;
    }
}
