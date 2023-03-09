package xyz.spruceloader.trunk.exceptions;

public class LoadingException extends RuntimeException {
    public LoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadingException(String message) {
        super(message);
    }
}
