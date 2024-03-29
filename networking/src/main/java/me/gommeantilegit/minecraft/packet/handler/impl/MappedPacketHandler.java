package me.gommeantilegit.minecraft.packet.handler.impl;

import io.netty.channel.ChannelHandlerContext;
import me.gommeantilegit.minecraft.packet.Packet;
import me.gommeantilegit.minecraft.packet.handler.PacketHandler;
import me.gommeantilegit.minecraft.utils.async.SchedulableThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MappedPacketHandler<T extends Packet> extends PacketHandler<T> {

    /**
     * Stores the parent packet handler for the given packetID
     */
    @NotNull
    private final Map<Short, PacketHandler<? extends T>> handlerMap = new ConcurrentHashMap<>();

    public MappedPacketHandler(@Nullable SchedulableThread thread) {
        super(thread);
    }

    /**
     * Invoked object creation to register all packet handlers that are mapped in {@link #handlerMap}
     */
    public abstract void registerStaticPacketHandlers();

    /**
     * @param id            the id of the packet that the specified decoder decodes
     * @param packetHandler the packetHandler to handle the packet
     */
    public <T1 extends T> void registerPacketHandler(short id, @NotNull PacketHandler<T1> packetHandler) {
        this.handlerMap.put(id, packetHandler);
    }

    @SuppressWarnings({"unchecked cast", "rawtypes"})
    @Override
    public void handlePacket(@NotNull T packet, @NotNull ChannelHandlerContext context) {
        PacketHandler handler = this.handlerMap.get(packet.getPacketID());
        if (handler != null) {
            SchedulableThread thread = handler.getThread();
            if (thread == null) {
                handler.handlePacket(packet, context);
            } else {
                Runnable task = () -> handler.handlePacket(packet, context);;
                thread.scheduleTask(task);
            }
        }
    }
}
