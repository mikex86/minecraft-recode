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
import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static me.gommeantilegit.minecraft.Side.SERVER;

@SideOnly(side = SERVER)
public class ServerChunkLoader extends ChunkLoaderBase implements IPacketListener<ClientPacket> {

    /**
     * Parent minecraft instance
     */
    @NotNull
    private final ServerMinecraft mc;

    /**
     * Executor service for async chunk generation
     */
    @NotNull
    private final ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable runnable) {
            Thread thread = new Thread(runnable, "ChunkLoadingWorker");
            thread.setDaemon(true);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
            return thread;
        }
    });

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
    }

    @Override
    public void onPacketReceived(@NotNull ClientPacket packet, @NotNull Channel channel) {
        NetHandlerPlayServer netHandler = mc.nettyServer.netHandlerPlayServer;
        ChannelData channelData = netHandler.getData(channel);
        if (packet instanceof ClientRequestChunkDataPacket) { // Chunk Data Request
            Vector2 origin = ((ClientRequestChunkDataPacket) packet).getChunkOrigin();
            channelData.sendPacket(new ServerChunkRequestConfrimationPacket(null, origin));
            this.service.submit(() -> {
                ServerChunk chunk = ((ServerWorld) world).getChunkCreator().tryCreateChunkFor(new Vec2i(origin));
                // Waiting for world generation to finish as another thread has just requested this chunk before, which means it has invoke world generation. We want it too, so we wait
                while (!chunk.isWorldGenerationFinished()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ServerChunkDataPacket chunkData = new ServerChunkDataPacket(null, chunk.getChunkOrigin().asLibGDXVec2D(), chunk);
                channelData.sendPacket(chunkData);
            });
        }
    }
}
