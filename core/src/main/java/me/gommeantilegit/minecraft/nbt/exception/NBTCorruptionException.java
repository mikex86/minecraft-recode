package me.gommeantilegit.minecraft.nbt.exception;

public class NBTCorruptionException extends RuntimeException {

    public NBTCorruptionException() {
    }

    public NBTCorruptionException(String message) {
        super(message);
    }

    public NBTCorruptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NBTCorruptionException(Throwable cause) {
        super(cause);
    }

    public NBTCorruptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
