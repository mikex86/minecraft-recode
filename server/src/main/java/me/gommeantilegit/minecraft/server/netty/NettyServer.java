package me.gommeantilegit.minecraft.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.handler.NetHandlerPlayServer;
import me.gommeantilegit.minecraft.packet.proc.impl.dec.ClientPacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.impl.enc.ServerPacketEncoder;
import org.jetbrains.annotations.NotNull;

public class NettyServer extends Thread {

    /**
     * Port of the server connection
     */
    private final int port;

    /**
     * Server minecraft instance
     */
    @NotNull
    private final ServerMinecraft mc;

    @NotNull
    public NetHandlerPlayServer netHandlerPlayServer;

    public NettyServer(int port, @NotNull ServerMinecraft mc) {
        super("Netty-Server-Thread");
        this.port = port;
        this.mc = mc;
        this.netHandlerPlayServer = new NetHandlerPlayServer(mc); // Client packets handler;
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer() {

                            @Override
                            protected void initChannel(Channel ch) {
                                ch.pipeline().
                                        addLast("framer", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4)).
                                        addLast("framer-prepender", new LengthFieldPrepender(4, false)).
                                        addLast(
                                                new ClientPacketDecoder(mc), // Ingoing packets are all client packets
                                                new ServerPacketEncoder(), // Outgoing packets are all server packets
                                                netHandlerPlayServer
                                        );
                            }
                        }).childOption(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture f = b.bind(port).sync();
                f.sync().channel().closeFuture().sync();
            } catch (InterruptedException e) {
                this.mc.getLogger().info("Netty Server Thread interrupted");
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * Sends the given packet to the specified client
     *
     * @param packet  the packet to be sent
     * @param channel the client channel
     */
    public void sendPacket(@NotNull ServerPacket packet, @NotNull Channel channel) {
        channel.writeAndFlush(packet);
    }

}
