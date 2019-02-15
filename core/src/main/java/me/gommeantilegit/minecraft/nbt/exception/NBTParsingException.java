package me.gommeantilegit.minecraft.nbt.exception;

public class NBTParsingException extends RuntimeException{

    public NBTParsingException() {
    }

    public NBTParsingException(String message) {
        super(message);
    }

    public NBTParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public NBTParsingException(Throwable cause) {
        super(cause);
    }

    public NBTParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
