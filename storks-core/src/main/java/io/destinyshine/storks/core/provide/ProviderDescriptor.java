package io.destinyshine.storks.core.provide;

import io.destinyshine.storks.core.ServiceInfoDescriptor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * pure java bean.
 *
 * @author destinyliu
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProviderDescriptor<T> extends ServiceInfoDescriptor<T> {

    private final String[] protocol;

    private final T serviceObject;

    public ProviderDescriptor(Class<T> serviceInterface, String serviceVersion, String[] protocol, T serviceObject) {
        super(serviceInterface, serviceVersion);
        this.protocol = protocol;
        this.serviceObject = serviceObject;
    }

    public ProviderDescriptor(Class<T> serviceInterface, String serviceVersion, String protocol, T serviceObject) {
        super(serviceInterface, serviceVersion);
        this.protocol = new String[]{protocol};
        this.serviceObject = serviceObject;
    }

    public ProviderDescriptor(Class<T> serviceInterface, String serviceVersion, T serviceObject) {
        super(serviceInterface, serviceVersion);
        this.protocol = new String[]{};
        this.serviceObject = serviceObject;
    }

}
