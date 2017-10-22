package io.destinyshine.storks.http;

/**
 * @author liujianyu
 */
public class HttpStatusCodeException extends RuntimeException {

    private int statusCode;

    public HttpStatusCodeException() {
    }

    public HttpStatusCodeException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
