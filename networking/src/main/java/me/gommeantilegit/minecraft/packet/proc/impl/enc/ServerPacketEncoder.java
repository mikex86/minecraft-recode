package me.gommeantilegit.minecraft.packet.proc.impl.enc;

import me.gommeantilegit.minecraft.packet.ClientPacket;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.packet.packets.server.ServerRequestUserInfoPacket;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import me.gommeantilegit.minecraft.packet.proc.impl.MappedPacketEncoder;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;


/**
 * Mapped encoder for server side packets. Mapping is reflection automated
 */
public class ServerPacketEncoder extends MappedPacketEncoder<ServerPacket> {

    /**
     * Walking over server packet classes (all classes in {@link me.gommeantilegit.minecraft.packet.packets.server}) and registering their encoders
     */
    @Override
    protected void registerPacketEncoders() {
        Reflections serverPackets = new Reflections(ServerRequestUserInfoPacket.class.getPackage().getName());
        Set<Class<? extends ServerPacket>> serverPacketClasses = serverPackets.getSubTypesOf(ServerPacket.class);
        for (Class<? extends ServerPacket> packetClass : serverPacketClasses) {
            if (packetClass.isAnnotationPresent(PacketInfo.class)) {
                PacketInfo packetInfo = packetClass.getAnnotation(PacketInfo.class);
                if (packetInfo.side() != PacketInfo.PacketSide.SERVER)
                    throw new RuntimeException("Server Packet Class PacketInfo is assigned wrong side. CLIENT --> should be SERVER!");
                Class<? extends PacketEncoder> encoderClass = packetInfo.encoder();
                try {
                    Constructor<? extends PacketEncoder> constructor = encoderClass.getConstructor();
                    try {
                        short packetID;
                        try {
                            packetID = (short) packetClass.getField("PACKET_ID").get(null);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Server Packet class " + packetClass.getName() + "'s PACKET_ID field could not be accessed!");
                        } catch (NoSuchFieldException e) {
                            throw new RuntimeException("Server Packet class " + packetClass.getName() + " must have a field of type short called PACKET_ID!");
                        }
                        if ((packetID & ClientPacket.CLIENT_PACKET_BIT) == 0) {
                            registerPacketEncoder(packetID, constructor.newInstance());
                        } else {
                            throw new RuntimeException("Server Packet class " + packetClass.getName() + " packetID have the CLIENT_PACKET_BIT set!");
                        }
                    } catch (InstantiationException e) {
                        throw new RuntimeException("Server Packet Encoder " + encoderClass.getName() + "'s constructor could not be instantiated!");
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Server Packet Encoder " + encoderClass.getName() + "'s constructor could not be accessed!");
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("Server Packet Encoder " + encoderClass.getName() + "'s constructor threw an exception on invocation.", e);
                    }
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Server Packet Encoder " + encoderClass.getName() + " must have an empty constructor!");
                }
            } else {
                throw new RuntimeException("Server Packet Class " + packetClass.getName() + " is not annotated with PacketInfo annotation!");
            }
        }
    }
}
