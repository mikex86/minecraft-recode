package me.gommeantilegit.minecraft.block.state;

import me.gommeantilegit.minecraft.block.BlockBase;
import me.gommeantilegit.minecraft.block.state.property.BlockStatePropertyMap;
import me.gommeantilegit.minecraft.block.state.property.impl.EnumBlockStateProperty;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of IBlockState
 */
public class BlockStateBase implements IBlockState {

    /**
     * The enum facing block state property that stores the facing direction of the block
     */
    @NotNull
    public static final EnumBlockStateProperty<EnumFacing> ENUM_FACING = new EnumBlockStateProperty<>("enum_facing", EnumFacing.class);

    /**
     * The block instance
     */
    private BlockBase block;

    /**
     * The property map connecting properties with their parent values
     */
    @NotNull
    private final BlockStatePropertyMap propertyMap;

    public BlockStateBase(@NotNull BlockBase block, @NotNull EnumFacing facing) {
        this.block = block;
        BlockStatePropertyMap.Builder builder = new BlockStatePropertyMap.Builder();
        builder.withProperty(ENUM_FACING, facing);
        this.propertyMap = builder.build();
    }

    private BlockStateBase(@NotNull BlockBase block, @NotNull BlockStatePropertyMap propertyMap) {
        this.block = block;
        this.propertyMap = propertyMap;
    }

    @Override
    @NotNull
    public BlockBase getBlock() {
        return block;
    }

    @NotNull
    @Override
    public EnumFacing getFacing() {
        return this.propertyMap.getPropertyValue(ENUM_FACING).getValue();
    }

    @Override
    public void setFacing(@NotNull EnumFacing facing) {
        this.propertyMap.setPropertyValue(ENUM_FACING, facing);
    }

    @Override
    public void setBlock(@NotNull BlockBase block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return "BlockState{blockName: " + block.getName() + ", id: " + block.getId() + ", Facing: " + getFacing().name() + "}";
    }

    /**
     * @return a new BlockState instance with the same block type and an equal property map (new instance of the property map but same values and keys).
     */
    @NotNull
    public BlockStateBase copySelf() {
        return new BlockStateBase(this.block, this.propertyMap.copySelf());
    }
}
