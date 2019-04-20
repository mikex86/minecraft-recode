package me.gommeantilegit.minecraft.block.state;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.block.ServerBlock;
import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.api.INBTConverter;
import me.gommeantilegit.minecraft.nbt.exception.NBTParsingException;
import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.nbt.impl.NBTInteger;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerBlockState extends BlockStateBase<ServerBlock> {

    /**
     * NBT Converter instance
     */
    public static final NBTConverter NBT_CONVERTER = new NBTConverter();

    public ServerBlockState(@NotNull ServerBlock block, @NotNull EnumFacing facing) {
        super(block, facing);
    }

    @Override
    public ServerBlockState clone() {
        return new ServerBlockState(getBlock(), getFacing());
    }

    public static final class NBTConverter implements INBTConverter<NBTArray, ServerBlockState> {

        @NotNull
        @Override
        public NBTArray toNBTData(ServerBlockState object) {
            return new NBTArray(new NBTObject[]{
                    new NBTInteger(object == null ? 0 : object.getBlock().getId()),
                    new NBTInteger(object == null ? 0 : object.getFacing().ordinal())
            });
        }

        /**
         * @param object the NBTArray to be parsed into an instance of IBlockState
         * @param args   extra arguments if needed
         * @return the IBlockState instance represented by the NBTArray passed in
         * @throws NBTParsingException if parsing fails
         * @throws AssertionError      if args[0] is not the server's minecraft instance
         * @apiNote args[0] must be the server minecraft instance! else assertion error will be Thrown
         */
        @Nullable
        @Override
        public ServerBlockState fromNBTData(NBTArray object, Object... args) throws NBTParsingException {
            assert args.length > 0;
            assert args[0] instanceof AbstractMinecraft;
            try {
                int blockID = ((NBTInteger) object.getValue()[0]).getValue();
                int facingOrdinal = ((NBTInteger) object.getValue()[1]).getValue();
                ServerBlock block = ((ServerMinecraft) args[0]).blocks.getBlockByID(blockID);
                EnumFacing facing = EnumFacing.values()[facingOrdinal];
                if (block == null)
                    return null;
                return new ServerBlockState(block, facing);
            } catch (Exception e) {
                throw new NBTParsingException("Parsing IBlockState from NBTData failed!", e);
            }
        }

    }
}
