package me.gommeantilegit.minecraft.packet.handler.chunk;

import com.badlogic.gdx.math.Vector2;
import io.netty.channel.ChannelHandlerContext;
import me.gommeantilegit.minecraft.packet.handler.NetHandlerPlayClient;
import me.gommeantilegit.minecraft.packet.handler.PacketHandler;
import me.gommeantilegit.minecraft.packet.packets.client.ClientChunkLoadConfirmPacket;
import me.gommeantilegit.minecraft.packet.packets.server.ServerChunkDataPacket;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static me.gommeantilegit.minecraft.utils.io.IOUtils.decompress;

public class ClientChunkDataHandler extends PacketHandler<ServerChunkDataPacket> {

    @NotNull
    private final NetHandlerPlayClient netHandlerPlayClient;

    public ClientChunkDataHandler(@NotNull NetHandlerPlayClient netHandlerPlayClient) {
        this.netHandlerPlayClient = netHandlerPlayClient;
    }

    @Override
    public void handlePacket(@NotNull ServerChunkDataPacket packet, @NotNull ChannelHandlerContext context) {
        int worldHeight = packet.getWorldHeight();
        assert worldHeight == netHandlerPlayClient.mc.theWorld.getHeight();
        Vector2 origin = packet.getChunkOrigin();
        ClientChunk chunk = netHandlerPlayClient.mc.theWorld.getChunkAtOrigin((int) origin.x, (int) origin.y);
        if (chunk != null) {
            assert chunk.getHeight() == packet.getWorldHeight();
            assert chunk.getChunkSections().size() == packet.getChunkSectionsSent().length;
            CompletableFuture.runAsync(() -> {
                try {
                    chunk.setChunkData(decompress(packet.getChunkData()), packet.getChunkSectionsSent());
//                    chunk.getChunkSections().forEach(s -> ((ClientChunkSection) s).deleteMesh());
                    chunk.setDataReceived();
                    chunk.load();
                    context.channel().writeAndFlush(new ClientChunkLoadConfirmPacket(null, origin));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}

