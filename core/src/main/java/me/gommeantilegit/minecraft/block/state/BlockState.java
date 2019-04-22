package me.gommeantilegit.minecraft.block.state;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.state.property.BlockStateProperty;
import me.gommeantilegit.minecraft.block.state.property.BlockStatePropertyMap;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.utils.serialization.exception.DeserializationException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Implementation of IBlockState
 */
public class BlockState implements IBlockState {

    /**
     * The block instance
     */
    private Block block;

    /**
     * The property map connecting properties with their parent values
     */
    @NotNull
    private final BlockStatePropertyMap propertyMap;

    public BlockState(@NotNull Block block) {
        this.block = block;
        BlockStatePropertyMap.Builder builder = new BlockStatePropertyMap.Builder();
        initProperties(builder);
        this.propertyMap = builder.build();
    }

    /**
     * @see BlockStatePropertyMap#getPropertyValue(BlockStateProperty)
     * @throws IllegalStateException if BlockStatePropertyMap#getPropertyValue(BlockStateProperty) returns an empty optional
     */
    @NotNull
    public <T> T getPropertyValue(@NotNull BlockStateProperty<T> property) {
        return this.propertyMap.getPropertyValue(property).orElseThrow(() -> new IllegalStateException("Cannot find property " + property + "in property map " + propertyMap + " of block state of block type " + block));
    }

    /**
     * @see BlockStatePropertyMap#setPropertyValue(BlockStateProperty, Object)
     */
    public <T> boolean setValue(BlockStateProperty<T> property, T value) {
        return this.propertyMap.setPropertyValue(property, value);
    }

    /**
     * Method can be overridden to add properties to the property map of the block state
     *
     * @param builder the builder building the property map instance
     */
    protected void initProperties(@NotNull BlockStatePropertyMap.Builder builder) {
    }

    private BlockState(@NotNull Block block, @NotNull BlockStatePropertyMap propertyMap) {
        this.block = block;
        this.propertyMap = propertyMap;
    }

    @Override
    @NotNull
    public Block getBlock() {
        return block;
    }

    @Override
    public void setBlock(@NotNull Block block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return "BlockState{blockName: " + block.getUnlocalizedName() + ", id: " + block.getId() + "}";
    }


    @Override
    public void serialize(@NotNull BitByteBuffer buffer) {
        buffer.writeUnsignedShort(this.block.getId());
    }

    @NotNull
    @Override
    public BlockState deserialize(@NotNull BitByteBuffer buffer, @NotNull AbstractMinecraft mc) throws DeserializationException {
        int blockID = buffer.readUnsignedShort();
        if (blockID == 0) {
            throw new DeserializationException("Read blockID is 0! Air does not have a block state!");
        }
        return new BlockState(Objects.requireNonNull(mc.blocks.getBlockByID(blockID)));
    }

    @NotNull
    public BlockState copySelf() {
        return new BlockState(this.block, this.propertyMap.copySelf());
    }

    @NotNull
    public BlockStatePropertyMap getPropertyMap() {
        return propertyMap;
    }
}
