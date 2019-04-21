package me.gommeantilegit.minecraft.block.state.property.impl;

import me.gommeantilegit.minecraft.block.state.property.BlockStateProperty;
import org.jetbrains.annotations.NotNull;

public class EnumBlockStateProperty<T extends Enum> extends BlockStateProperty<T> {

    public EnumBlockStateProperty(@NotNull String name, @NotNull Class<T> valueClass) {
        super(name, valueClass);
    }

    @NotNull
    @Override
    public String getName() {
        return super.getName();
    }
}
