package io.destinyshine.storks.core;

/**
 * @author destinyliu
 */
public class NoProviderDefinedException extends RuntimeException {

    public NoProviderDefinedException() {
    }

    public NoProviderDefinedException(String message) {
        super(message);
    }

    public NoProviderDefinedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoProviderDefinedException(Throwable cause) {
        super(cause);
    }

    public NoProviderDefinedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
