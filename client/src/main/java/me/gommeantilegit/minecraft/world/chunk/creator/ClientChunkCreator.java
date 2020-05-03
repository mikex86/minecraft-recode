package me.gommeantilegit.minecraft.world.chunk.creator;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.WorldBase;
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
    public ClientChunk createChunk(int chunkX, int chunkZ, WorldBase world) {
        ClientChunk chunk = new ClientChunk(world.getHeight(), chunkX, chunkZ, (ClientWorld) world, ((ClientWorld) world).getChunkMeshRebuilder());
        createChunk(chunk);
        return chunk;
    }

}
