package me.gommeantilegit.minecraft.utils.serialization.exception;

/**
 * Exception thrown when serialization of an object fails
 */
public class SerializationException extends Exception {

    public SerializationException() {
    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }
}
