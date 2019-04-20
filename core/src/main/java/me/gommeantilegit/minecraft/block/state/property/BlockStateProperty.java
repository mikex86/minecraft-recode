package me.gommeantilegit.minecraft.block.state.property;

import org.jetbrains.annotations.NotNull;

public class BlockStateProperty<T> {

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

    public BlockStateProperty(@NotNull String name, @NotNull Class<T> valueClass) {
        this.name = name;
        this.valueClass = valueClass;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BlockStateProperty) {
            return ((BlockStateProperty) o).name.equals(name) && ((BlockStateProperty) o).valueClass == valueClass;
        }
        return false;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
