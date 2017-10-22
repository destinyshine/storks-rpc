package io.destinyshine.storks.registry.consul.client;

/**
 * @author liujianyu
 */
public abstract class ConsulAccessor {

    protected String host;

    protected int port;

    public ConsulAccessor(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }

}
