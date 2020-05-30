package me.gommeantilegit.minecraft.block.state;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.utils.serialization.exception.DeserializationException;
import me.gommeantilegit.minecraft.utils.serialization.exception.SerializationException;
import org.jetbrains.annotations.NotNull;

public interface IBlockState {

    /**
     * @return the block of the state
     */
    @NotNull
    Block getBlock();

    @Override
    String toString();

//    /**
//     * Called to serialize the block state on the byte buffer. Super must be called as the super writes the block id of the block state to the buffer!
//     *
//     * @param buffer the byte buffer to serialize the block state on - the buffer that the data used to serialize the BlockState instance is written to.
//     * @throws SerializationException if serialization fails
//     */
//    void serialize(@NotNull BitByteBuffer buffer) throws SerializationException;
//
//    /**
//     * Called to deserialize the data of the specified buffer into a block state instance. Only super returned value must be modified!
//     *
//     * @param buffer the buffer to read the data from - the buffer to deserialize the object from
//     * @param blocks the parent blocks instance
//     * @return the de-serialized instance - an instance that is constructed from the data read from the buffer
//     * @throws DeserializationException if deserialization fails
//     */
//    @NotNull
//    IBlockState deserialize(@NotNull BitByteBuffer buffer, @NotNull Blocks blocks) throws DeserializationException;

    /**
     * @return a new BlockState instance with the same block type and an equal property map (new instance of the property map but same values and keys).
     */
    @NotNull
    BlockState copySelf();
}
