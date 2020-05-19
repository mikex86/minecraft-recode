package me.gommeantilegit.minecraft.world.chunk.creator;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.block.state.palette.IBlockStatePalette;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;

public class ClientChunkCreator extends ChunkCreatorBase {

    @NotNull
    private final ClientMinecraft mc;

    public ClientChunkCreator(@NotNull ClientMinecraft mc, @NotNull ClientWorld world) {
        super(world);
        this.mc = mc;
    }

    @NotNull
    @Override
    protected ClientChunk newChunkFor(int height, int chunkX, int chunkZ, WorldBase world, IBlockStatePalette blockStatePalette) {
        return new ClientChunk(world.getHeight(), chunkX, chunkZ, (ClientWorld) world, ((ClientWorld) world).getChunkMeshRebuilder(), world.getBlockStatePalette());
    }

    @NotNull
    @Override
    public synchronized ClientChunk createChunk(@NotNull Vec2i origin) {
        return (ClientChunk) super.createChunk(origin);
    }

    @NotNull
    @Override
    public ClientChunk createChunk(int chunkX, int chunkZ, WorldBase world) {
        return (ClientChunk) super.createChunk(chunkX, chunkZ, world);
    }

    @NotNull
    @Override
    public synchronized ClientChunk tryCreateChunkFor(@NotNull Vec2i position) {
        return (ClientChunk) super.tryCreateChunkFor(position);
    }
}
