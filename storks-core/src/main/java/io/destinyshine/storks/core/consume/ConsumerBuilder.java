package io.destinyshine.storks.core.consume;

/**
 * @author destinyliu
 */
public class ConsumerBuilder<T> {

    private Class<T> serviceInterface;
    private String serviceVersion;

    private String remoteServer;
    private int remotePort;
    private boolean direct = false;

    private ConsumerBuilder() {

    }

    public static <T> ConsumerBuilder<T> ofServiceInterface(Class<T> serviceType) {
        ConsumerBuilder<T> consumerBuilder;
        consumerBuilder = new ConsumerBuilder<>();
        consumerBuilder.serviceInterface = serviceType;
        return consumerBuilder;
    }

    public Class<T> serviceInterface() {
        return serviceInterface;
    }

    public String serviceVersion() {
        return serviceVersion;
    }

    public ConsumerBuilder<T> serviceVersion(String version) {
        this.serviceVersion = version;
        return this;
    }

    public ConsumerBuilder<T> remoteServer(String remoteServer) {
        this.remoteServer = remoteServer;
        return this;
    }

    public ConsumerBuilder<T> direct(boolean direct) {
        this.direct = direct;
        return this;
    }

    public ConsumerBuilder<T> remotePort(int remotePort) {
        this.remotePort = remotePort;
        return this;
    }

    public ConsumerDescriptor<T> build() {
        ConsumerDescriptor<T> desc = new ConsumerDescriptor<>(
            serviceInterface,
            serviceVersion,
            remoteServer,
            remotePort,
            direct
        );
        return desc;
    }

}
