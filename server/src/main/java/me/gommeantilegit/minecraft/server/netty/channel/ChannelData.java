package me.gommeantilegit.minecraft.server.netty.channel;

import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerMP;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.packets.server.ServerForceClientChunkLoadingDistanceChangePacket;
import me.gommeantilegit.minecraft.utils.Clock;
import org.jetbrains.annotations.NotNull;

/**
 * Additional data parent to a channel
 */
public class ChannelData {

    @NotNull
    private final Channel parentChannel;

    /**
     * State whether the channel has established a valid session
     */
    private boolean hasValidSession = false;

    /**
     * The render distance of the player
     */
    private int chunkLoadingDistance;

    /**
     * The parent player instance
     */
    private EntityPlayerMP playerMP;

    /**
     * Tracks how long the player has been online
     */
    @NotNull
    private Clock onlineTimer = new Clock(false);

    public ChannelData(@NotNull Channel parentChannel) {
        this.parentChannel = parentChannel;
    }

    /**
     * Sets {@link #hasValidSession} to #state
     *
     * @param state the new state of {@link #hasValidSession}
     */
    public void setHasValidSession(boolean state) {
        this.hasValidSession = true;
        this.onlineTimer.reset();
    }

    public void setPlayerMP(@NotNull EntityPlayerMP playerMP) {
        this.playerMP = playerMP;
    }

    @NotNull
    public EntityPlayerMP getPlayerMP() {
        if (playerMP == null)
            throw new IllegalStateException("Channel " + parentChannel + " does not have a parent player!");
        return playerMP;
    }

    /**
     * @see #hasValidSession
     */
    public boolean hasValidSession() {
        return hasValidSession;
    }

    public int getChunkLoadingDistance() {
        return chunkLoadingDistance;
    }

    /**
     * Sets the render / chunk loading distance of the channel to the specified value without sending a packet to the client
     * to perform that change as well
     *
     * @param chunkLoadingDistance the render / chunk loading distance specified
     */
    public void modifyChunkLoadingDistance(int chunkLoadingDistance) {
        this.chunkLoadingDistance = chunkLoadingDistance;
    }

    @NotNull
    public Channel getParentChannel() {
        return parentChannel;
    }

    /**
     * Sends the given packet to the client
     *
     * @param packet the given packet
     */
    public void sendPacket(@NotNull ServerPacket packet) {
        this.parentChannel.writeAndFlush(packet);
    }

    /**
     * Sets the render distance of the channel to the specified value and sends a packet to the client to perform that change as well
     *
     * @param chunkLoadingDistance the specified render distance
     */
    public void setChunkLoadingDistance(int chunkLoadingDistance) {
        modifyChunkLoadingDistance(chunkLoadingDistance);
        sendPacket(new ServerForceClientChunkLoadingDistanceChangePacket(null, chunkLoadingDistance));
    }

    /**
     * @see #onlineTimer
     */
    public long getTimeOnline() {
        return this.onlineTimer.getTimePassed();
    }
}
