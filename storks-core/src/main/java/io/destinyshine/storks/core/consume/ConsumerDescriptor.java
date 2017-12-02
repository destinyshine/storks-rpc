package io.destinyshine.storks.core.consume;

import io.destinyshine.storks.core.ServiceInfoDescriptor;
import lombok.Getter;

/**
 * 消费者描述信息
 * <p>
 * 只提供消费者的描述，不作任何其他操作，纯粹的java bean。
 * </p>
 *
 * @author liujianyu
 */
@Getter
public class ConsumerDescriptor<T> extends ServiceInfoDescriptor {

    public static final int DEFAULT_CONNECTIONS_NUM = 1;

    private final String remoteHost;
    private final int remotePort;
    private final boolean direct;
    private final int connectionsNum;

    public ConsumerDescriptor(Class<T> serviceInterface,
                              String serviceVersion,
                              String remoteHost,
                              int remotePort,
                              boolean direct) {
        super(serviceInterface, serviceVersion);
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.direct = direct;
        this.connectionsNum = DEFAULT_CONNECTIONS_NUM;
    }

    public ConsumerDescriptor(Class<T> serviceInterface, String serviceVersion, String remoteHost, int remotePort,
                              boolean direct, int connectionsNum) {
        super(serviceInterface, serviceVersion);
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.direct = direct;
        this.connectionsNum = connectionsNum;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof ConsumerDescriptor) {
            ConsumerDescriptor other = (ConsumerDescriptor)obj;
            return other.getServiceInterface().equals(this.serviceInterface)
                && other.getServiceVersion().equals(other.getServiceVersion());
        }
        return false;
    }
}
