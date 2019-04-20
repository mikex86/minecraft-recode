package me.gommeantilegit.minecraft.packet.annotations;

import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to store packet information
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PacketInfo {

    /**
     * The side of minecraft implementation that sends this packet
     */
    @NotNull
    PacketSide side();

    /**
     * The decoder class of the packet
     */
    @NotNull
    Class<? extends PacketDecoder> decoder();

    /**
     * The encoder class of the packet
     */
    @NotNull
    Class<? extends PacketEncoder> encoder();

    /**
     * Packet Side
     */
    enum PacketSide {
        CLIENT, SERVER
    }

}
