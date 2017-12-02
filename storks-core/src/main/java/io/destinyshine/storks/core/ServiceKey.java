package io.destinyshine.storks.core;

import java.util.Objects;

import io.destinyshine.storks.core.consume.ConsumerDescriptor;
import io.destinyshine.storks.core.provide.ProviderDescriptor;

/**
 * Created by liujianyu.ljy on 17/8/19.
 *
 * @author liujianyu
 * @date 2017/08/19
 */
public class ServiceKey {

    private final String serviceInterface;

    private final String serviceVersion;

    public ServiceKey(String serviceInterface, String serviceVersion) {
        this.serviceInterface = serviceInterface;
        this.serviceVersion = serviceVersion;
    }

    public static ServiceKey of(String serviceInterface, String serviceVersion) {
        return new ServiceKey(serviceInterface, serviceVersion);
    }

    public static ServiceKey of(ProviderDescriptor desc) {
        return new ServiceKey(desc.getServiceInterface().getName(), desc.getServiceVersion());
    }

    public static ServiceKey of(ConsumerDescriptor<?> desc) {
        return new ServiceKey(desc.getServiceInterface().getName(), desc.getServiceVersion());
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ServiceKey)) {
            return false;
        } else {
            ServiceKey sk = (ServiceKey)obj;
            return Objects.equals(sk.serviceInterface, this.serviceInterface)
                && Objects.equals(sk.serviceVersion, this.serviceVersion);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceInterface, serviceVersion);
    }

    @Override
    public String toString() {
        return serviceInterface + ":" + serviceVersion;
    }
}
