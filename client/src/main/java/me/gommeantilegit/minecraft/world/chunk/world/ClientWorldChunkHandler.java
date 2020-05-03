package me.gommeantilegit.minecraft.world.chunk.world;

import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.ClientBlockRendererTypeRegistry;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.builder.ChunkMeshRebuilder;
import me.gommeantilegit.minecraft.world.chunk.creator.ChunkCreatorBase;
import org.jetbrains.annotations.NotNull;

@SideOnly(side = Side.CLIENT)
public class ClientWorldChunkHandler extends WorldChunkHandlerBase {

    @NotNull
    public final ChunkMeshRebuilder chunkMeshRebuilder;

    public ClientWorldChunkHandler(@NotNull ChunkCreatorBase chunkCreator,  @NotNull ClientWorld clientWorld, @NotNull ClientBlockRendererTypeRegistry rendererRegistry) {
        super();
        this.chunkMeshRebuilder = new ChunkMeshRebuilder(clientWorld, rendererRegistry);
    }

    @NotNull
    public ChunkMeshRebuilder getChunkMeshRebuilder() {
        return chunkMeshRebuilder;
    }
}
