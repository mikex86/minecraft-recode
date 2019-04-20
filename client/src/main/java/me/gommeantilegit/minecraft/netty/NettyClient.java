package me.gommeantilegit.minecraft.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.packet.ClientPacket;
import me.gommeantilegit.minecraft.packet.handler.NetHandlerPlayClient;
import me.gommeantilegit.minecraft.packet.packets.client.ClientRequestChunkDataPacket;
import me.gommeantilegit.minecraft.packet.proc.impl.dec.ServerPacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.impl.enc.ClientPacketEncoder;
import org.jetbrains.annotations.NotNull;

/**
 * Netty client connection thread
 */
public class NettyClient extends Thread {

    /**
     * Host to connect to
     */
    @NotNull
    private final String hostAddress;

    /**
     * Port for connection
     */
    private final int port;

    /**
     * Minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * NetHandlerPlayClient instance
     */
    @NotNull
    public final NetHandlerPlayClient netHandlerPlayClient;

    /**
     * Server Connection channel instance
     */
    public Channel channel;

    public NettyClient(@NotNull String hostAddress, int port, @NotNull ClientMinecraft mc) {
        setName("Netty-Client-Thread");
        setDaemon(true);
        this.hostAddress = hostAddress;
        this.mc = mc;
        this.port = port;
        this.netHandlerPlayClient = new NetHandlerPlayClient(mc); // Client side Packet Handler
    }

    @Override
    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer() {

                @Override
                protected void initChannel(Channel ch) {
                    ch.pipeline().
                            addLast("framer", new LengthFieldBasedFrameDecoder(65536, 0, 2, 0, 2)).
                            addLast("framer-prepender", new LengthFieldPrepender(2, false)).
                            addLast(
                                    new ServerPacketDecoder(mc), // Incoming Packets are server packets
                                    new ClientPacketEncoder(), // Outgoing Packets are client packets
                                    netHandlerPlayClient
                            );
                    channel = ch;
                }
            });

            try {
                ChannelFuture f = b.connect(hostAddress, port).sync();
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                mc.logger.info("Netty Client Thread interrupted");
            }
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public int getPort() {
        return port;
    }

    @NotNull
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * Sends the given packet to the server
     *
     * @param packet the given packet
     */
    public void sendPacket(@NotNull ClientPacket packet) {
        if (channel != null)
            this.channel.writeAndFlush(packet);
    }
}
