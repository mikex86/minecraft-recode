package me.gommeantilegit.minecraft.world.chunk.loader;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerMP;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.packet.ClientPacket;
import me.gommeantilegit.minecraft.packet.handler.IPacketListener;
import me.gommeantilegit.minecraft.packet.handler.NetHandlerPlayServer;
import me.gommeantilegit.minecraft.packet.packets.client.ClientRequestChunkDataPacket;
import me.gommeantilegit.minecraft.packet.packets.server.ServerChunkDataPacket;
import me.gommeantilegit.minecraft.packet.packets.server.ServerChunkRequestConfrimationPacket;
import me.gommeantilegit.minecraft.server.netty.channel.ChannelData;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.util.VectorUtils;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static me.gommeantilegit.minecraft.Side.SERVER;

@SideOnly(side = SERVER)
public class ServerChunkLoader extends ChunkLoaderBase implements AsyncOperation, IPacketListener<ClientPacket> {

    /**
     * Parent minecraft instance
     */
    @NotNull
    private final ServerMinecraft mc;

    /**
     * @param world sets {@link #world}
     * @param mc    sets {@link #mc}
     */
    public ServerChunkLoader(@NotNull ServerWorld world, @NotNull ServerMinecraft mc) {
        super(world);
        this.mc = mc;
    }

    /**
     * Updates the chunkLoader. Loads / unloads chunk during this call, if necessary
     **/
    @Override
    public void onAsyncThread() {
        {
            ArrayList<ChunkBase> chunks = new ArrayList<>(this.world.getWorldChunkHandler().getChunks());
            List<PlayerBase> viewers = world.getChunkCreator().getViewers();
            for (int i1 = 0, viewersSize = viewers.size(); i1 < viewersSize; i1++) {
                PlayerBase viewer = viewers.get(i1);
                Vector2 playerPosition; //2D vector storing the viewers x and y coordinates
                {
                    Vector3 viewingPosition = viewer.getPositionVector();
                    playerPosition = new Vector2(viewingPosition.x, viewingPosition.z);
                }
                for (ChunkBase chunk : chunks) {

                    double distance; // Distance for chunk to viewer
                    {
                        Vector2 chunkPosition = chunk.getChunkOrigin().asLibGDXVec2D();
                        distance = playerPosition.dst(chunkPosition);
                    }
                    if (distance < ((EntityPlayerMP) viewer).channelData.getChunkLoadingDistance()) {
                        if (!chunk.isLoaded()) {
                            chunk.load();
                        }
                    } else {
                        // If chunk is loaded and no player is near it (in chunk loading distance of it) --> unload it
                        if (chunk.isLoaded() && viewers.stream().allMatch(v -> VectorUtils.xzTo2D(v.getPositionVector()).dst(chunk.getChunkOrigin().asLibGDXVec2D()) > ((EntityPlayerMP) v).channelData.getChunkLoadingDistance())) {
                            chunk.unload();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPacketReceived(@NotNull ClientPacket packet, @NotNull Channel channel) {
        NetHandlerPlayServer netHandler = mc.nettyServer.netHandlerPlayServer;
        ChannelData channelData = netHandler.getData(channel);
        if (packet instanceof ClientRequestChunkDataPacket) { // Chunk Data Request
            Vector2 origin = ((ClientRequestChunkDataPacket) packet).getChunkOrigin();
            channelData.sendPacket(new ServerChunkRequestConfrimationPacket(null, origin));
            ChunkBase chunk = world.getChunkAtOrigin((int) origin.x, (int) origin.y);
            if (chunk != null) {
                ServerChunkDataPacket chunkData = new ServerChunkDataPacket(null, chunk.getChunkOrigin().asLibGDXVec2D(), chunk);
                channelData.sendPacket(chunkData);
            } else {
                world.getChunkCreator().tryCreateChunkFor(new Vec2i(origin));
                ChunkBase createdChunk = world.getChunkAtOrigin((int) origin.x, (int) origin.y);
                if (createdChunk == null)
                    throw new IllegalStateException("ChunkCreator failed to create chunk at origin " + origin + " after explicitly requesting it to create one for this very origin. This is a fatal bug!");
                ServerChunkDataPacket chunkData = new ServerChunkDataPacket(null, createdChunk.getChunkOrigin().asLibGDXVec2D(), createdChunk);
                channelData.sendPacket(chunkData);
            }
        }
    }
}
