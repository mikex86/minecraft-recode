package me.gommeantilegit.minecraft.block.log;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.material.Materials;
import me.gommeantilegit.minecraft.block.state.property.impl.EnumBlockStateProperty;
import org.jetbrains.annotations.NotNull;

public class BlockLog extends Block {

    /**
     * The property storing the direction that the log block is facing in
     */
    @NotNull
    public static final EnumBlockStateProperty<LogFacing> LOG_FACING = new EnumBlockStateProperty<>("facing", LogFacing.class, LogFacing.NONE);

    public BlockLog(@NotNull String name, int id) {
        super(name, id, Materials.wood);
        this.setHardness(2.0f);
    }

    @Override
    protected void onRegisterProperties() {
        this.registerProperty(LOG_FACING);
    }

    /**
     * Represents the possible facing directions of the log block
     */
    public enum LogFacing {
        X, Y, Z, NONE
    }
}
