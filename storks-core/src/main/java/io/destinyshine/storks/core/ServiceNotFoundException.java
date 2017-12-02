package io.destinyshine.storks.core;

/**
 * Created by liujianyu.ljy on 17/8/29.
 *
 * @author liujianyu
 * @date 2017/08/29
 */
public class ServiceNotFoundException extends RuntimeException {

    public ServiceNotFoundException() {
    }

    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotFoundException(Throwable cause) {
        super(cause);
    }

    public ServiceNotFoundException(String message, Throwable cause, boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
