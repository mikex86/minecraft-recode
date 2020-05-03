package me.gommeantilegit.minecraft.packet.handler;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.handler.chunk.ClientChunkDataHandler;
import me.gommeantilegit.minecraft.packet.handler.impl.MappedPacketHandler;
import me.gommeantilegit.minecraft.packet.packets.client.ClientHandshakePacket;
import me.gommeantilegit.minecraft.packet.packets.client.ClientChunkLoadingDistanceChangePacket;
import me.gommeantilegit.minecraft.packet.packets.client.ClientUserInfoPacket;
import me.gommeantilegit.minecraft.packet.packets.server.*;
import me.gommeantilegit.minecraft.ui.screen.impl.GuiConnectionFailed;
import me.gommeantilegit.minecraft.world.ClientWorld;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static me.gommeantilegit.minecraft.world.chunk.ChunkSection.CHUNK_SECTION_SIZE;

/**
 * Handler object handling incoming server packets
 */
public class NetHandlerPlayClient extends MappedPacketHandler<ServerPacket> {

    @NotNull
    public final ClientMinecraft mc;

    /**
     * State if the session is validated by the server
     */
    private boolean sessionEstablished;

    /**
     * State whether the world is already setup meaning that {@link #mc} {@link ClientMinecraft#theWorld} is not null
     */
    private boolean worldSetup = false;

    public NetHandlerPlayClient(@NotNull ClientMinecraft mc) {
        super(null); // The Thread where the handler tasks are scheduled is the netty thread. Thus this argument is null
        this.mc = mc;
        registerStaticPacketHandlers();
    }

    /**
     * Contains all registered channels
     */
    @NotNull
    private final List<Channel> registeredChannels = new ArrayList<>();

    @Override
    public void registerStaticPacketHandlers() {

        registerPacketHandler(ServerRequestUserInfoPacket.PACKET_ID, new PacketHandler<ServerRequestUserInfoPacket>() {

            @Override
            public void handlePacket(@NotNull ServerRequestUserInfoPacket packet, @NotNull ChannelHandlerContext context) {
                context.channel().writeAndFlush(new ClientUserInfoPacket(null, mc.thePlayer.getUsername(), mc.thePlayer.getSkin()));
            }

        });

        registerPacketHandler(ServerPositionSetPacket.PACKET_ID, new PacketHandler<ServerPositionSetPacket>(mc.getMinecraftThread()) {
            @Override
            public void handlePacket(@NotNull ServerPositionSetPacket packet, @NotNull ChannelHandlerContext context) {
                if (packet.hasPosition()) {
                    Vector3 pos = packet.getPosition();
                    assert pos != null;
                    float x = pos.x, y = pos.y, z = pos.z;
                    mc.thePlayer.setPosition(x, y, z);
                }
                if (packet.hasRotation()) {
                    Vector2 rot = packet.getRotation();
                    assert rot != null;
                    mc.thePlayer.setRotation(rot.x, rot.y);
                }
            }
        });

        registerPacketHandler(ServerRequestRenderDistance.PACKET_ID, new PacketHandler<ServerRequestRenderDistance>(mc.getMinecraftThread()) {
            @Override
            public void handlePacket(@NotNull ServerRequestRenderDistance packet, @NotNull ChannelHandlerContext context) {
                context.channel().writeAndFlush(new ClientChunkLoadingDistanceChangePacket(null, mc.theWorld.getChunkLoadingDistance()));
            }
        });

        registerPacketHandler(ServerForceClientChunkLoadingDistanceChangePacket.PACKET_ID, new PacketHandler<ServerForceClientChunkLoadingDistanceChangePacket>() {

            @Override
            public void handlePacket(@NotNull ServerForceClientChunkLoadingDistanceChangePacket packet, @NotNull ChannelHandlerContext context) {
                mc.theWorld.setChunkLoadingDistance(packet.getRenderDistance());
            }

        });

        registerPacketHandler(ServerSessionValidationConfirmationPacket.PACKET_ID, new PacketHandler<ServerSessionValidationConfirmationPacket>() // Processed on netty thread as the change must occur instantly
        {
            @Override
            public void handlePacket(@NotNull ServerSessionValidationConfirmationPacket packet, @NotNull ChannelHandlerContext context) {
                setSessionEstablished(true);
            }
        });

        registerPacketHandler(ServerWorldSetupPacket.PACKET_ID, new PacketHandler<ServerWorldSetupPacket>(mc.getMinecraftThread()) {
            @Override
            public void handlePacket(@NotNull ServerWorldSetupPacket packet, @NotNull ChannelHandlerContext context) {
                assert packet.getWorldHeight() % CHUNK_SECTION_SIZE == 0;
                mc.theWorld = new ClientWorld(mc.thePlayer, mc, packet.getWorldHeight()); // Initializing the world
                // Setting the world time to the value that the world time on the server would have progressed to assuming steady fluent tick rate
                mc.theWorld.worldTime = packet.getWorldTime() + (new Date().getTime() - packet.getPacketSentUnixTime()) / 20;
                mc.theWorld.getChunkLoader().addViewer(mc.thePlayer);
                mc.theWorld.spawnEntityInWorld(mc.thePlayer); // Spawning player

                setWorldSetup(true);
            }
        });

        registerPacketHandler(ServerDisconnectMessagePacket.PACKET_ID, new PacketHandler<ServerDisconnectMessagePacket>() {
            @Override
            public void handlePacket(@NotNull ServerDisconnectMessagePacket packet, @NotNull ChannelHandlerContext context) {
                mc.getLogger().err("Server terminated connection. Reason: " + packet.getMessage());
                mc.uiManager.displayGuiScreen(new GuiConnectionFailed("Failed to connect to the server", packet.getMessage()));
            }
        });

        registerPacketHandler(ServerChunkDataPacket.PACKET_ID, new ClientChunkDataHandler(this));
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ServerPacket msg) {
        try {
//            if (msg instanceof ServerSessionValidationConfirmationPacket) {
//                System.out.println("ServerSessionValidationConfirmationPacket");
//            }
            if ((isSessionEstablished()) || !msg.isGamePacket())
                super.messageReceived(ctx, msg);
            else
                System.out.println("Ignored packet: " + msg);
        } catch (Throwable t) {
            this.mc.getLogger().exception("Exception on server packet processing caught!", t);
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        Channel channel = ctx.channel();
        this.registeredChannels.add(channel);
        this.mc.getLogger().debug("Channel " + channel.remoteAddress() + " registered");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.channel().writeAndFlush(new ClientHandshakePacket(null));
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        this.registeredChannels.remove(ctx.channel());
        this.mc.getLogger().debug("Channel " + ctx.channel().remoteAddress() + " unregistered");
        mc.closeServerConnection();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        mc.uiManager.displayGuiScreen(new GuiConnectionFailed("Connection Lost", cause.getMessage()));
        cause.printStackTrace();
    }

    @NotNull
    public List<Channel> getRegisteredChannels() {
        return registeredChannels;
    }

    public boolean isSessionEstablished() {
        return sessionEstablished;
    }

    public void setSessionEstablished(boolean sessionEstablished) {
        this.sessionEstablished = sessionEstablished;
    }

    public boolean isWorldSetup() {
        return worldSetup;
    }

    private void setWorldSetup(boolean state) {
        this.worldSetup = true;
    }
}
