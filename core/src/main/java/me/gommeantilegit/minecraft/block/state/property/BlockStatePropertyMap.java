package me.gommeantilegit.minecraft.block.state.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Stores Properties with their parent values.
 * Properties are added on construction of object.
 * Properties are immutable and cannot be added or removed.
 */
public class BlockStatePropertyMap {

    /**
     * The properties of the map
     */
    @NotNull
    private final List<BlockStateProperty> properties;

    /**
     * The parent values of the properties.
     * No value of this array should ever be null!
     * <p>
     * valueFor(properties[x]) = values[x]
     */
    @NotNull
    private final Object[] values;

    /**
     * @param properties the properties that the map should assign to variables
     * @param values     the index parent values for the specified property map elements
     */
    private BlockStatePropertyMap(@NotNull BlockStateProperty[] properties, @NotNull Object[] values) {
        assert properties.length == values.length;
        this.properties = Arrays.asList(properties);
        this.values = values;
    }

    /**
     * @param property the given property instance
     * @param <T>      the value type of the property instance
     * @return an optional that has a nullable value. Returns an empty Optional if the property was not found in the map. Else the function returns an optional storing the value of the specified property, which cannot be null!
     */
    @NotNull
    public <T> Optional<T> getPropertyValue(@NotNull BlockStateProperty<T> property) {
        int index = properties.indexOf(property);
        if (index == -1)
            return Optional.empty();
        assert index < values.length;
        return Optional.of((T) this.values[index]);
    }

    /**
     * Sets the parent value of the specified property to the specified value
     *
     * @param property the specified property
     * @param value    the new value for the specified property
     * @param <T>      the type of value that is stored in the specified property
     * @return the true if the value was successfully changed and false if the property was not found in the property map or if the specified value is not an allowed value of the property
     */
    public <T> boolean setPropertyValue(@NotNull BlockStateProperty<T> property, @NotNull T value) {
        int index = properties.indexOf(property);
        assert properties.get(index).getValueClass().equals(property.getValueClass()); // This is always true
        if (index == -1 || !((BlockStateProperty<T>) properties.get(index)).isValidValue(value))
            return false;
        assert index < values.length;
        values[index] = value;
        return true;
    }

    /**
     * @return an equal instance of this instance. Same property instances and same value instance are stored in this new map.
     */
    @NotNull
    public BlockStatePropertyMap copySelf() {
        return new BlockStatePropertyMap(properties.toArray(new BlockStateProperty[0]), values);
    }

    /**
     * @return true if the property map does not contain any properties
     */
    public boolean isEmpty() {
        return this.properties.isEmpty();
    }

    /**
     * Builder object for {@link BlockStatePropertyMap}
     */
    public static class Builder {

        /**
         * The properties for the {@link BlockStatePropertyMap} instance
         */
        @NotNull
        private final List<BlockStateProperty> properties = new ArrayList<>();

        /**
         * The index parent values of the specified properties in {@link #properties}
         * <p>
         * valueFor(properties[x]) = values[x]
         */
        @NotNull
        private final List<Object> values = new ArrayList<>();

        /**
         * @param property     the property to add
         * @param initialValue the initial value for the property. Can be null.
         * @param <T>          the type of value that the property stores
         * @return self instance for method call chaining
         */
        @NotNull
        public <T> Builder withProperty(@NotNull BlockStateProperty<T> property, @NotNull T initialValue) {
            synchronized (this) {
                this.properties.add(property);
                this.values.add(initialValue);
                return this;
            }
        }

        /**
         * @return the {@link BlockStatePropertyMap} instance built by the builder
         */
        @NotNull
        public BlockStatePropertyMap build() {
            synchronized (this) {
                return new BlockStatePropertyMap(this.properties.toArray(new BlockStateProperty[0]), this.values.toArray());
            }
        }

    }

}
