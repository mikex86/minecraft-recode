package me.gommeantilegit.minecraft.packet.exception;

/**
 * Exception thrown when encoding of a packet fails
 */
public class PacketEncodingException extends Exception {

    public PacketEncodingException() {
    }

    public PacketEncodingException(String message) {
        super(message);
    }

    public PacketEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacketEncodingException(Throwable cause) {
        super(cause);
    }

    public PacketEncodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
