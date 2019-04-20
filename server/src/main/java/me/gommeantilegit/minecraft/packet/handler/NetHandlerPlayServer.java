package me.gommeantilegit.minecraft.packet.handler;

import io.netty.channel.*;
import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerMP;
import me.gommeantilegit.minecraft.packet.ClientPacket;
import me.gommeantilegit.minecraft.packet.handler.impl.MappedPacketHandler;
import me.gommeantilegit.minecraft.packet.handler.response.IPacketResponseListener;
import me.gommeantilegit.minecraft.packet.packets.client.ClientHandshakePacket;
import me.gommeantilegit.minecraft.packet.packets.client.ClientMovePacket;
import me.gommeantilegit.minecraft.packet.packets.client.ClientChunkLoadingDistanceChangePacket;
import me.gommeantilegit.minecraft.packet.packets.client.ClientUserInfoPacket;
import me.gommeantilegit.minecraft.packet.packets.server.*;
import me.gommeantilegit.minecraft.server.netty.channel.ChannelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Packet handler for the server to handle incoming packets from the clients
 */
@ChannelHandler.Sharable
public class NetHandlerPlayServer extends MappedPacketHandler<ClientPacket> {

    /**
     * 10 seconds of timeout delay for packet responses
     */
    private static final long RESPONSE_TIMEOUT = 10_000;

    /**
     * Parent server minecraft instance
     */
    @NotNull
    private final ServerMinecraft mc;

    /**
     * Stores all channel data parent to the corresponding channels
     */
    @NotNull
    public final HashMap<Channel, ChannelData> channelData = new HashMap<>();

    /**
     * Contains all registered channels
     */
    @NotNull
    private final List<Channel> registeredChannels = new ArrayList<>();

    public NetHandlerPlayServer(@NotNull ServerMinecraft mc) {
        super(mc.minecraftThread);
        this.mc = mc;
        registerStaticPacketHandlers();
        this.addListener(mc.theWorld.getChunkLoader());
    }

    @Override
    public void registerStaticPacketHandlers() {
        registerPacketHandler(ClientMovePacket.PACKET_ID, new PacketHandler<ClientMovePacket>(mc.minecraftThread) {
            @Override
            public void handlePacket(@NotNull ClientMovePacket packet, @NotNull ChannelHandlerContext context) {
                ChannelData channelData = getData(context.channel());
                channelData.getPlayerMP().queueMovePacket(packet);
            }
        });
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        registerChannel(ctx.channel());

        this.expectPacketResponse(ctx.channel(), RESPONSE_TIMEOUT, ClientHandshakePacket.class, new IPacketResponseListener<ClientHandshakePacket>() {

            @Override
            public void onTimeOut(@NotNull Channel channel) {
                channel.writeAndFlush(new ServerDisconnectMessagePacket(null, "ClientHandshake Packet not sent!"));
                channel.close();
            }

            @Override
            public void onPacketReceived(@NotNull ClientHandshakePacket packet, @NotNull Channel channel) {
                channel.writeAndFlush(new ServerRequestUserInfoPacket(null));
                expectPacketResponse(channel, RESPONSE_TIMEOUT, ClientUserInfoPacket.class, new IPacketResponseListener<ClientUserInfoPacket>() {

                    @Override
                    public void onTimeOut(@NotNull Channel channel) {
                        channel.writeAndFlush(new ServerDisconnectMessagePacket(null, "ClientUserInfoPacket not sent!"));
                        channel.close();
                    }

                    @Override
                    public void onPacketReceived(@NotNull ClientUserInfoPacket packet, @NotNull Channel channel) {
                        ChannelData channelData = getData(channel);
                        channelData.setHasValidSession(true);

                        EntityPlayerMP entityPlayerMP = new EntityPlayerMP(mc.theWorld, 20, packet.getUserName(), packet.getSkin(), channel);
                        channelData.setPlayerMP(entityPlayerMP);
                        mc.theWorld.spawnEntityInWorld(entityPlayerMP);
                        mc.theWorld.getChunkCreator().addViewer(entityPlayerMP);
                        channel.writeAndFlush(new ServerWorldSetupPacket(null, mc.theWorld));

                        channel.writeAndFlush(new ServerRequestRenderDistance(null));
                        expectPacketResponse(channel, RESPONSE_TIMEOUT, ClientChunkLoadingDistanceChangePacket.class, new IPacketResponseListener<ClientChunkLoadingDistanceChangePacket>() {

                            @Override
                            public void onTimeOut(@NotNull Channel channel) {
                                channel.writeAndFlush(new ServerDisconnectMessagePacket(null, "ClientRenderDistanceChangePacket not sent!"));
                                channel.close();
                            }

                            @Override
                            public void onPacketReceived(@NotNull ClientChunkLoadingDistanceChangePacket renderDistanceChangePacket, @NotNull Channel channel) {
                                if (renderDistanceChangePacket.getRenderDistance() <= mc.configuration.getMaxChunkLoadingDistance())
                                    channelData.modifyChunkLoadingDistance(renderDistanceChangePacket.getRenderDistance());
                                else channelData.setChunkLoadingDistance(mc.configuration.getMaxChunkLoadingDistance());
                                channel.writeAndFlush(new ServerSessionValidationConfirmationPacket(null));
                                channelData.getPlayerMP().setPositionAndRotation(mc.theWorld.getSpawnPoint().x, mc.theWorld.getSpawnPoint().y, mc.theWorld.getSpawnPoint().z, 0, 0);
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ClientPacket msg) {
        // Protecting the server from malicious clients
        try {
            ChannelData channelData = getData(ctx.channel());
            if (channelData.hasValidSession() || !msg.isGamePacket())
                super.messageReceived(ctx, msg);
            else {
                // A Client should never do that and thus it is malicious
                // TODO: MESSAGE
                ctx.channel().close();
            }
        } catch (Throwable t) {
            this.mc.logger.exception("Exception caught on netty thread while processing " + ctx.channel() + "'s packet", t);
            t.printStackTrace();
            // TODO: MESSAGE
            ctx.channel().close();
        }
    }

    /**
     * Registers the specified channel by adding it to the {@link #registeredChannels} list and creating a {@link ChannelData} instance parent to the channel which is stored in {@link #channelData}
     *
     * @param channel the specified channel
     */
    public void registerChannel(@NotNull Channel channel) {
        this.registeredChannels.add(channel);
        this.channelData.put(channel, new ChannelData(channel));
        this.mc.logger.debug("Channel " + channel.remoteAddress() + " registered");
    }

    /**
     * Unregisters the specified channel by removing it from the {@link #channelData} list and removing its channel specific data from {@link #channelData}
     * Also removes the parent player entity from the world by killing it of.
     *
     * @param channel the specified channel
     */
    public void unregisterChannel(@NotNull Channel channel) {
        this.registeredChannels.remove(channel);
        this.getData(channel).getPlayerMP().setDead();
        this.channelData.remove(channel);
        this.mc.logger.debug("Channel " + channel.remoteAddress() + " unregistered");
    }

    @NotNull
    public ChannelData getData(@NotNull Channel channel) {
        ChannelData channelData = this.channelData.get(channel);
        if (channelData == null) {
            throw new IllegalStateException("No ChannelData found for " + channel + "! This should never happen!");
        }
        return channelData;
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        unregisterChannel(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    }

    @NotNull
    public List<Channel> getRegisteredChannels() {
        return registeredChannels;
    }

    /**
     * @param playerName the name of the player
     * @return the {@link EntityPlayerMP} instance with that name
     */
    @Nullable
    public EntityPlayerMP getPlayerByName(@NotNull String playerName) {
        List<ChannelData> values = new ArrayList<>(this.channelData.values());
        for (int i = 0; i < values.size(); i++) {
            ChannelData data = values.get(i);
            if (data.getPlayerMP().getUsername().equals(playerName)) {
                return data.getPlayerMP();
            }
        }
        return null;
    }
}
