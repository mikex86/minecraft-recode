package me.gommeantilegit.minecraft.packet.exception;

/**
 * Exception thrown when packet decoding fails
 */
public class PacketDecodingException extends Exception {

    public PacketDecodingException() {
    }

    public PacketDecodingException(String message) {
        super(message);
    }

    public PacketDecodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacketDecodingException(Throwable cause) {
        super(cause);
    }

    public PacketDecodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
