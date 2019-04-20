package me.gommeantilegit.minecraft.utils.serialization.exception;

/**
 * An Exception thrown when de-serialization of an object fails
 */
public class DeserializationException extends Exception {

    public DeserializationException() {
    }

    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeserializationException(Throwable cause) {
        super(cause);
    }
}
