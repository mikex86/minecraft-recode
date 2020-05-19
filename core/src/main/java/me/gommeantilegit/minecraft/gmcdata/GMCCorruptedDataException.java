package me.gommeantilegit.minecraft.gmcdata;

/**
 * Thrown to indicate corrupted GMC data
 */
public class GMCCorruptedDataException extends RuntimeException {

    public GMCCorruptedDataException() {
    }

    public GMCCorruptedDataException(String message) {
        super(message);
    }

    public GMCCorruptedDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public GMCCorruptedDataException(Throwable cause) {
        super(cause);
    }

    public GMCCorruptedDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
