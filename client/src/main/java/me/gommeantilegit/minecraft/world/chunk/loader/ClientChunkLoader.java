package me.gommeantilegit.minecraft.world.chunk.loader;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.packet.packets.client.ClientChunkLoadConfirmPacket;
import me.gommeantilegit.minecraft.packet.packets.client.ClientChunkUnloadPacket;
import me.gommeantilegit.minecraft.packet.packets.client.ClientRequestChunkDataPacket;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.gommeantilegit.minecraft.Side.CLIENT;

@SideOnly(side = CLIENT)
public class ClientChunkLoader extends ChunkLoaderBase<ClientChunk, ClientWorld> {

    /**
     * Client Minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * Queue of all chunks which data has already been requested
     */
    @NotNull
    private final ArrayList<ClientChunk> requested = new ArrayList<>();

    /**
     * @param world sets {@link #world}
     */
    public ClientChunkLoader(@NotNull ClientWorld world) {
        super(world);
        this.mc = world.mc;
    }

    @Override
    public void onAsyncThread() {
        super.onAsyncThread();
        Vector2 playerPosition; //2D vector storing the viewers x and y coordinates
        {
            Vector3 viewingPosition = mc.thePlayer.getPositionVector();
            playerPosition = new Vector2(viewingPosition.x, viewingPosition.z);
        }

        List<ClientChunk> chunks = this.world.getWorldChunkHandler().getChunks();
        for (ClientChunk chunk : chunks) {
            float distance = chunk.getChunkOrigin().asLibGDXVec2D().dst(playerPosition);
            if (distance < world.getChunkLoadingDistance()) {

                if (!world.getWorldChunkHandler().getNearChunks().contains(chunk))
                    world.getWorldChunkHandler().addNearChunk(chunk);

                if (!chunk.isLoaded()) {
                    mc.nettyClient.sendPacket(new ClientChunkLoadConfirmPacket(null, chunk.getChunkOrigin().asLibGDXVec2D()));
                    chunk.load();
                    if (!requested.contains(chunk))
                        requestChunkData(chunk);
                }

            } else {
                if (world.getWorldChunkHandler().getNearChunks().contains(chunk))
                    world.getWorldChunkHandler().removeNearChunk(chunk);
                if (chunk.isLoaded()) {
                    chunk.unload();
                    mc.nettyClient.sendPacket(new ClientChunkUnloadPacket(null, chunk.getChunkOrigin().asLibGDXVec2D()));
                }
            }
        }
    }

    /**
     * Sends a packet to the server to request the block data of the specified chunk
     *
     * @param chunk the specified chunk
     */
    private void requestChunkData(@NotNull ClientChunk chunk) {
        mc.nettyClient.sendPacket(new ClientRequestChunkDataPacket(null, chunk.getChunkOrigin().asLibGDXVec2D()));
        this.requested.add(chunk);
    }
}
