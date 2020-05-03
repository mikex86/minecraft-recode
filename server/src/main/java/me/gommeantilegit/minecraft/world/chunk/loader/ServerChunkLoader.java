package me.gommeantilegit.minecraft.world.chunk.loader;

import com.badlogic.gdx.math.Vector2;
import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerMP;
import me.gommeantilegit.minecraft.packet.ClientPacket;
import me.gommeantilegit.minecraft.packet.handler.IPacketListener;
import me.gommeantilegit.minecraft.packet.handler.NetHandlerPlayServer;
import me.gommeantilegit.minecraft.packet.packets.client.ClientRequestChunkDataPacket;
import me.gommeantilegit.minecraft.packet.packets.server.ServerChunkDataPacket;
import me.gommeantilegit.minecraft.packet.packets.server.ServerChunkRequestConfrimationPacket;
import me.gommeantilegit.minecraft.server.netty.channel.ChannelData;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;

import static me.gommeantilegit.minecraft.Side.SERVER;

@SideOnly(side = SERVER)
public class ServerChunkLoader extends ChunkLoaderBase implements IPacketListener<ClientPacket> {

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


    @Override
    public int getChunkLoadingDistance(@NotNull Entity ent) {
        return ent instanceof EntityPlayerMP ? ((EntityPlayerMP) ent).getChannelData().getChunkLoadingDistance() : world.getChunkLoadingDistance();
    }

    @Override
    public void tick(float partialTicks) {
//        {
//            Stream<Reference<ChunkBase>> unloadedChunks = this.world.getWorldChunkHandler().getLoadableChunks();
//            List<ChunkBase> loadedChunks = this.world.getWorldChunkHandler().getLoadedChunks();
//            List<PlayerBase> viewers = this.world.getChunkCreator().getViewers();
//            if (viewers.isEmpty()) {
//                for (ChunkBase loadedChunk : loadedChunks) {
//                    System.out.println("Unloading server chunk");
//                    loadedChunk.unload();
//                }
//                return;
//            }
//            for (PlayerBase viewer : viewers) {
//                Vector2 playerPosition; //2D vector storing the viewers x and y coordinates
//                {
//                    Vector3 viewingPosition = viewer.getPositionVector();
//                    playerPosition = new Vector2(viewingPosition.x, viewingPosition.z);
//                }
//                List<Reference<ChunkBase>> deletedReferences = new ArrayList<>();
//                unloadedChunks.forEach(reference -> {
//                    ChunkBase chunk = reference.get();
//                    if (chunk == null) {
//                        deletedReferences.add(reference);
//                        return;
//                    }
//                    updateUnloadedChunk(chunk, playerPosition, viewer);
//                });
//                for (Reference<ChunkBase> deletedReference : deletedReferences) {
////                    this.world.getWorldChunkHandler().reportDeletedChunkReference(deletedReference);
//                }
//            }
//            for (ChunkBase chunk : loadedChunks) {
//                // If chunk is loaded and no player is near it (in chunk loading distance of it) --> unload it
//                boolean shouldUnload = true;
//                for (PlayerBase viewer : viewers) {
//                    Vector2 topDownPos = VectorUtils.xzTo2D(viewer.getPositionVector());
//                    if (topDownPos.dst(chunk.getChunkOrigin().asLibGDXVec2D()) <= ((EntityPlayerMP) viewer).getChannelData().getChunkLoadingDistance()) {
//                        shouldUnload = false;
//                    }
//                }
//                if (shouldUnload) {
//                    System.out.println("Unloading server chunk");
//                    chunk.unload();
//                }
//            }
//        }
    }

//    private void updateUnloadedChunk(@NotNull ChunkBase chunk, @NotNull Vector2 playerPosition, @NotNull PlayerBase viewer) {
//        double distance; // Distance for chunk to viewer
//        {
//            Vector2 chunkPosition = chunk.getChunkOrigin().asLibGDXVec2D();
//            distance = playerPosition.dst(chunkPosition);
//        }
//        if (distance < ((EntityPlayerMP) viewer).getChannelData().getChunkLoadingDistance()) {
//            if (!chunk.isLoaded()) {
//                System.out.println("Loading server chunk");
//                chunk.load();
//            }
//        }
//    }

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
                chunk = world.getChunkCreator().tryCreateChunkFor(new Vec2i(origin));
                ServerChunkDataPacket chunkData = new ServerChunkDataPacket(null, chunk.getChunkOrigin().asLibGDXVec2D(), chunk);
                channelData.sendPacket(chunkData);
            }
        }
    }
}
