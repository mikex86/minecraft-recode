package me.gommeantilegit.minecraft.entity.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.entity.player.base.skin.SkinBase;
import me.gommeantilegit.minecraft.packet.packets.client.ClientMovePacket;
import me.gommeantilegit.minecraft.packet.packets.server.ServerPositionSetPacket;
import me.gommeantilegit.minecraft.server.netty.channel.ChannelData;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class EntityPlayerMP extends PlayerBase<SkinBase> {

    /**
     * Move packets received from the client that need to be processed
     */
    @NotNull
    private final Queue<ClientMovePacket> movePackets = new LinkedList<>();

    /**
     * Parent channel data
     */
    @NotNull
    public final ChannelData channelData;

    /**
     * State if move packets need to be processed
     */
    private boolean movePacketsToProcess = false;

    /**
     * @param world     sets {@link #world}
     * @param maxHealth sets {@link #maxHealth}
     * @param username  sets {@link #userName}
     * @param skin      sets {@link #skin}
     * @param channel   the parent channel that controls this player
     */
    public EntityPlayerMP(@NotNull ServerWorld world, int maxHealth, @NotNull String username, @NotNull SkinBase skin, @NotNull Channel channel) {
        super(world, maxHealth, username, skin);
        this.channelData = world.mc.nettyServer.netHandlerPlayServer.getData(channel);
    }

    /*
     * No updating server side as the player is controlled by packets. Thus physics are still applied to the player but client sided
     * Just processing move packets and adjusting player position and/or rotation accordingly
     */
    @Override
    public void onLivingUpdate() {
        if (movePacketsToProcess) {
            try {
                synchronized (movePackets) {
                    while (!movePackets.isEmpty()) {
                        ClientMovePacket packet = movePackets.remove();
                        processMovePacket(packet);
                    }
                }
            } catch (NoSuchElementException ignored) {
            }
            movePacketsToProcess = false;
        }
        if (posY < 0)
            setPosition(posX, 255, posZ);
    }

    /**
     * Processes the given move packet by adjusting the players position and rotation accordingly
     *
     * @param packet the packet to process
     */
    private void processMovePacket(@NotNull ClientMovePacket packet) {
        Vector3 pos = packet.getPosition();
        Vector2 rot = packet.getRotation();
        if (pos != null) {
            modifyPosition(pos.x, pos.y, pos.z);
        }
        if (rot != null) {
            modifyRotation(rot.x, rot.y);
        }
    }

    /**
     * Sets the rotation of the player server side
     *
     * @param yaw   the new yaw
     * @param pitch the new pitch
     */
    private void modifyRotation(float yaw, float pitch) {
        super.setRotation(yaw, pitch);
    }

    /**
     * Sets the players rotation by sending a position set packet to the client that processes this change
     *
     * @param yaw   the new yaw
     * @param pitch the new pitch
     */
    @Override
    public void setRotation(float yaw, float pitch) {
        this.channelData.sendPacket(new ServerPositionSetPacket(null, null, new Vector2(yaw, pitch)));
        super.setRotation(yaw, pitch);
    }

    /**
     * Modifies the player's position server side
     *
     * @param x the x component of the position vector
     * @param y the y component of the position vector
     * @param z the z component of the position vector
     */
    public void modifyPosition(float x, float y, float z) {
        super.setPosition(x, y, z);
    }

    /**
     * Sets the position of the multiplayer player by sending a position set packet that the client has to handle
     *
     * @param x the new x position component
     * @param y the new y position component
     * @param z the new z position component
     */
    @Override
    public void setPosition(float x, float y, float z) {
        this.channelData.sendPacket(new ServerPositionSetPacket(null, new Vector3(x, y, z), null));
        super.setPosition(x, y, z);
    }

    /**
     * Sets the position and rotation of the multiplayer player by sending a position set packet that the client has to handle
     *
     * @param x     the new x position component
     * @param y     the new y position component
     * @param z     the new z position component
     * @param yaw   the new rotation yaw
     * @param pitch the new rotation pitch
     */
    public void setPositionAndRotation(float x, float y, float z, float yaw, float pitch) {
//        System.out.println("setPositionAndRotation");
        this.channelData.getParentChannel().writeAndFlush(new ServerPositionSetPacket(null, new Vector3(x, y, z), new Vector2(yaw, pitch)));
    }

    public void queueMovePacket(@NotNull ClientMovePacket movePacket) {
        synchronized (movePackets) {
            this.movePackets.add(movePacket);
            this.movePacketsToProcess = true;
        }
    }
}
