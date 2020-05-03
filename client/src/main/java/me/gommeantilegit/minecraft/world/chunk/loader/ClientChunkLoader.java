package me.gommeantilegit.minecraft.world.chunk.loader;


import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerSP;
import me.gommeantilegit.minecraft.packet.packets.client.ClientChunkUnloadPacket;
import me.gommeantilegit.minecraft.packet.packets.client.ClientRequestChunkDataPacket;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static me.gommeantilegit.minecraft.Side.CLIENT;

@SideOnly(side = CLIENT)
public class ClientChunkLoader extends ChunkLoaderBase {

    /**
     * Client Minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * @param world sets {@link #world}
     * @param mc    parent minecraft instance
     */
    public ClientChunkLoader(@NotNull ClientWorld world, @NotNull ClientMinecraft mc) {
        super(world);
        this.mc = mc;
    }

    @Override
    public void tick(float partialTicks) {
    }

    /**
     * Sends a packet to the server to request the block data of the specified chunk
     *
     * @param chunk the specified chunk
     */
    private void requestChunkData(@NotNull ChunkBase chunk) {
        this.mc.nettyClient.sendPacket(new ClientRequestChunkDataPacket(null, chunk.getChunkOrigin().asLibGDXVec2D()));
    }

    /**
     * Sends a packet to the server to inform it that a chunk has been unloaded
     *
     * @param chunk the chunk that was unloaded
     */
    public void informUnload(@NotNull ChunkBase chunk) {
        this.mc.nettyClient.sendPacket(new ClientChunkUnloadPacket(null, chunk.getChunkOrigin().asLibGDXVec2D()));
    }

    @Override
    protected void load(@NotNull ChunkBase chunk, @NotNull Entity entity) {
        if (chunk.isLoaded())
            return;
        super.load(chunk, entity);
        this.requestChunkData(chunk);
    }

    @Override
    protected void unload(@NotNull ChunkBase chunk, @NotNull Entity entity) {
        super.unload(chunk, entity);
        this.informUnload(chunk);
    }

    @Override
    public int getChunkLoadingDistance(@NotNull Entity ent) {
        if (!(ent instanceof EntityPlayerSP)) {
            return 16; // entities should only spawn in already loaded chunks on the client
        }
        return this.world.getChunkLoadingDistance();
    }

}
