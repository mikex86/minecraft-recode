package me.gommeantilegit.minecraft.entity.player.packet;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerSP;
import me.gommeantilegit.minecraft.packet.packets.client.ClientMovePacket;
import org.jetbrains.annotations.NotNull;

/**
 * An object that sends packet to the server
 */
public class PacketSender {

    /**
     * Parent player
     */
    @NotNull
    private final EntityPlayerSP playerSP;

    /**
     * Server side player position
     */
    private Vector3 serverPosition;

    /**
     * Server side player rotation
     */
    private Vector2 serverRotation;

    public PacketSender(@NotNull EntityPlayerSP playerSP) {
        this.playerSP = playerSP;
    }

    /**
     * Sends move packets to the server
     */
    public void sendMovePackets() {
        boolean position = false;
        boolean rotation = false;
        block:
        {
            if (serverPosition == null) {
                serverPosition = playerSP.getPositionVector();
                position = true;
            }

            if (serverRotation == null) {
                serverRotation = new Vector2(playerSP.rotationYawTicked, playerSP.rotationPitchTicked);
                rotation = true;
            }
            if (position && rotation) break block;
            assert serverPosition != null;
            {
                Vector3 newPos = playerSP.getPositionVector();
                if (serverPosition.dst(newPos) > 0.25) {
                    serverPosition = newPos;
                    position = true;
                }
            }
            {
                Vector2 newRot = new Vector2(playerSP.rotationYawTicked, playerSP.rotationPitchTicked);
                if (serverRotation.dst(newRot) > 0.5) {
                    serverRotation = newRot;
                    rotation = true;
                }
            }
        }
        if (position || rotation)
            playerSP.mc.nettyClient.sendPacket(new ClientMovePacket(null, position ? serverPosition : null, rotation ? serverRotation : null));

    }

    public EntityPlayerSP getPlayerSP() {
        return playerSP;
    }
}
