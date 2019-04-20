package me.gommeantilegit.minecraft.packet.proc.impl.dec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.packet.ClientPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.packet.exception.PacketDecodingException;
import me.gommeantilegit.minecraft.packet.packets.client.ClientHandshakePacket;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.impl.MappedPacketDecoder;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

/**
 * Mapped decoder for client side packets. Mapping is reflection automated
 */
public class ClientPacketDecoder extends MappedPacketDecoder<ClientPacket> {

    public ClientPacketDecoder(@NotNull AbstractMinecraft mc) {
        super(mc);
    }

    /**
     * Walking over client packet classes (all classes in {@link me.gommeantilegit.minecraft.packet.packets.client}) and registering their decoders
     */
    @Override
    protected void registerPacketDecoders() {
        Reflections clientPackets = new Reflections(ClientHandshakePacket.class.getPackage().getName());
        Set<Class<? extends ClientPacket>> clientPacketClasses = clientPackets.getSubTypesOf(ClientPacket.class);
        for (Class<? extends ClientPacket> packetClass : clientPacketClasses) {
            if (packetClass.isAnnotationPresent(PacketInfo.class)) {
                PacketInfo packetInfo = packetClass.getAnnotation(PacketInfo.class);
                if (packetInfo.side() != PacketInfo.PacketSide.CLIENT)
                    throw new RuntimeException("Client Packet Class PacketInfo is assigned wrong side. SERVER --> should be CLIENT!");
                Class<? extends PacketDecoder> decoderClass = packetInfo.decoder();
                try {
                    Constructor<? extends PacketDecoder> constructor = decoderClass.getConstructor();
                    try {
                        short packetID;
                        try {
                            packetID = (short) packetClass.getField("PACKET_ID").get(null);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Client Packet class " + packetClass.getName() + "'s PACKET_ID field could not be accessed!");
                        } catch (NoSuchFieldException e) {
                            throw new RuntimeException("Client Packet class " + packetClass.getName() + " must have a field of type short called PACKET_ID!");
                        }
                        if ((packetID & ClientPacket.CLIENT_PACKET_BIT) != 0) {
                            registerPacketDecoder(packetID, constructor.newInstance());
                        } else {
                            throw new RuntimeException("Client Packet class " + packetClass.getName() + " packetID does not have the CLIENT_PACKET_BIT set!");
                        }
                    } catch (InstantiationException e) {
                        throw new RuntimeException("Client Packet Decoder " + decoderClass.getName() + "'s constructor could not be instantiated!");
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Client Packet Decoder " + decoderClass.getName() + "'s constructor could not be accessed!");
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("Client Packet Decoder " + decoderClass.getName() + "'s constructor threw an exception on invocation.", e);
                    }
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Client Packet Decoder " + decoderClass.getName() + " must have an empty constructor!");
                }
            } else {
                throw new RuntimeException("Client Packet Class " + packetClass.getName() + " is not annotated with PacketInfo annotation!");
            }
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        super.decode(ctx, in, out);
    }

    @Override
    public ClientPacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) throws PacketDecodingException {
        return super.deserialize(buffer, channel);
    }
}
