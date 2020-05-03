package me.gommeantilegit.minecraft.block.state.property;

import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a property that is parent to a {@link BlockState} instance
 *
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
    private final List<T> allowedValues;

    /**
     * The default value the property holds when not specified otherwise
     */
    @NotNull
    private final T defaultValue;

    public BlockStateProperty(@NotNull String name, @NotNull Class<T> valueClass, @NotNull List<T> allowedValues, @NotNull T defaultValue) {
        if (!isValidValue(defaultValue))
            throw new IllegalStateException("Default value for property not contained in allowed values!");
        this.name = name;
        this.valueClass = valueClass;
        this.allowedValues = allowedValues;
        this.defaultValue = defaultValue;
    }


    /**
     * Serializes a block state value of this property type onto a bit-buffer
     *
     * @param buffer        the destination buffer
     * @param propertyValue the value to serialize typed of this property
     */
    public void serialize(@NotNull BitByteBuffer buffer, @NotNull T propertyValue) {
        buffer.writeInt(this.allowedValues.indexOf(propertyValue));
    }

    /**
     * Deserializes a block state value of this property type from a bit-buffer
     *
     * @param buffer the source buffer
     * @return the de-serialized property value
     */
    @NotNull
    public T deserialize(@NotNull BitByteBuffer buffer) {
        return this.allowedValues.get(buffer.readInt());
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

    @NotNull
    public List<T> getAllowedValues() {
        return allowedValues;
    }

    @NotNull
    public T getDefaultValue() {
        return defaultValue;
    }
}
