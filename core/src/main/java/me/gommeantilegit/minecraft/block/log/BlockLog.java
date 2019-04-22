package me.gommeantilegit.minecraft.block.log;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.material.Materials;
import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.block.state.property.BlockStatePropertyMap;
import me.gommeantilegit.minecraft.block.state.property.impl.EnumBlockStateProperty;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.utils.serialization.exception.DeserializationException;
import org.jetbrains.annotations.NotNull;

public class BlockLog extends Block {

    /**
     * The property storing the direction that the log block is facing in
     */
    @NotNull
    public static final EnumBlockStateProperty<LogFacing> LOG_FACING = new EnumBlockStateProperty<>("facing", LogFacing.class);

    public BlockLog(@NotNull String name, int id) {
        super(name, id, Materials.wood);
        this.setHardness(2.0f);
    }

    @NotNull
    @Override
    public BlockState getDefaultBlockState() {
        return new BlockState(this) {
            @Override
            public void initProperties(@NotNull BlockStatePropertyMap.Builder builder) {
                builder.withProperty(LOG_FACING, LogFacing.NONE);
            }

            @Override
            public void serialize(@NotNull BitByteBuffer buffer) {
                super.serialize(buffer);
                buffer.writeByte((byte) getPropertyValue(LOG_FACING).ordinal());
            }

            @NotNull
            @Override
            public BlockState deserialize(@NotNull BitByteBuffer buffer, @NotNull AbstractMinecraft mc) throws DeserializationException {
                BlockState blockState = super.deserialize(buffer, mc);
                blockState.setValue(LOG_FACING, LogFacing.values()[buffer.readByte()]);
                return blockState;
            }
        };
    }

    /**
     * Represents the possible facing directions of the log block
     */
    public enum LogFacing {
        X, Y, Z, NONE
    }
}
