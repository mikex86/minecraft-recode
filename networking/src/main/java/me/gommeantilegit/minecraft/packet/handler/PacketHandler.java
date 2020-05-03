package me.gommeantilegit.minecraft.packet.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import me.gommeantilegit.minecraft.packet.Packet;
import me.gommeantilegit.minecraft.packet.handler.response.IPacketResponseListener;
import me.gommeantilegit.minecraft.packet.packets.server.ServerPositionSetPacket;
import me.gommeantilegit.minecraft.packet.packets.server.ServerSessionValidationConfirmationPacket;
import me.gommeantilegit.minecraft.utils.Pointer;
import me.gommeantilegit.minecraft.utils.async.SchedulableThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles all packets that can be received
 */
public abstract class PacketHandler<T extends Packet> extends SimpleChannelInboundHandler<T> {

    /**
     * Stores all active packet listeners
     */
    @NotNull
    private final List<IPacketListener<? extends T>> packetListeners = new CopyOnWriteArrayList<>();

    /**
     * Timer instance for scheduling delayed tasks
     */
    @NotNull
    protected final Timer timer = new Timer(true);

    /**
     * The Thread on which the packet listeners should be invoked.
     * Can be null - means the listeners are invoked on the netty thread.
     */
    @Nullable
    private final SchedulableThread thread;

    /**
     * @param thread sets {@link #thread}
     */
    protected PacketHandler(@Nullable SchedulableThread thread) {
        this.thread = thread;
    }

    protected PacketHandler() {
        this.thread = null;
    }

    /**
     * Handles the packet specified
     *
     * @param packet  the packet received
     * @param context supplied {@link ChannelHandlerContext}
     */
    public abstract void handlePacket(@NotNull T packet, @NotNull ChannelHandlerContext context);

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void messageReceived(ChannelHandlerContext ctx, T msg) {
        if (thread == null)
            handlePacket(msg, ctx);
        else thread.scheduleTask(() -> {
            if (ctx.channel().isOpen())
                handlePacket(msg, ctx);
            else System.out.println("IGNORED PACKET: " + msg);
        });

        for (IPacketListener packetListener : this.packetListeners) {
            if (packetListener instanceof AbstractPacketListener) {
                AbstractPacketListener listener = (AbstractPacketListener) packetListener;
                if (listener.getPacketClass().isAssignableFrom(msg.getClass())) {
                    if (listener.getChannel() != null) {
                        if (listener.getChannel() == ctx.channel()) {
                            invokePacketListener(ctx, msg, listener);
                        }
                    } else {
                        invokePacketListener(ctx, msg, packetListener);
                    }
                }
            } else {
                invokePacketListener(ctx, msg, packetListener);
            }
        }
    }

    private void invokePacketListener(ChannelHandlerContext ctx, T msg, IPacketListener<T> packetListener) {
        SchedulableThread thread = this.thread;
        if (packetListener instanceof PacketHandler) {
            thread = ((PacketHandler<?>) packetListener).getThread();
        }
        if (thread == null)
            packetListener.onPacketReceived(msg, ctx.channel());
        else
            thread.scheduleTask(() -> {
                if (ctx.channel().isOpen())
                    packetListener.onPacketReceived(msg, ctx.channel());
            });
    }

    /**
     * Registers the listener on the packet handler to be invoked
     *
     * @param listener the listener to be registered
     */
    public void addListener(@NotNull IPacketListener<? extends T> listener) {
        this.packetListeners.add(listener);
    }

    /**
     * Unregisters the listener from the packet handler
     *
     * @param listener the listener to be removed
     */
    public void removeListener(@NotNull AbstractPacketListener<? extends T> listener) {
        this.packetListeners.remove(listener);
    }

    /**
     * Registers a packet listener that is invoked when the expected packet was received or if the channel did not meet the timeout period
     *
     * @param channel                the channel that is expected to send the packet
     * @param timeOut                amount of time that the channel may take to respond in Milliseconds
     * @param packetClass            the class of type T1
     * @param packetResponseListener the listener to be invoked on result
     * @param <T1>                   the type of packet that is expected to be received by the channel
     */
    public <T1 extends T> void expectPacketResponse(@NotNull final Channel channel, long timeOut, Class<T1> packetClass, @NotNull final IPacketResponseListener<T1> packetResponseListener) {
        final Pointer<Boolean> packetReceived = new Pointer<>(false);
        AbstractPacketListener<T1> listener = new AbstractPacketListener<T1>(packetClass, channel) {
            @Override
            public void onPacketReceived(@NotNull T1 packet, @NotNull Channel channel) {
                packetResponseListener.onPacketReceived(packet, channel);
                packetReceived.value = true;
                removeListener(this);
            }
        };
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!packetReceived.value) {
                    removeListener(listener);
                    packetResponseListener.onTimeOut(channel);
                }
            }
        };
        this.timer.schedule(
                task,
                timeOut
        );
        addListener(listener);
    }

    @Nullable
    public SchedulableThread getThread() {
        return thread;
    }
}
