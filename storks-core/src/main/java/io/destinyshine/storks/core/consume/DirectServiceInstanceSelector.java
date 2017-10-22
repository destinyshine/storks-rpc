package io.destinyshine.storks.core.consume;

import java.util.Optional;

import io.destinyshine.storks.core.ServiceInstance;

/**
 * 只支持直连的服务选择器
 *
 * @author liujianyu
 * @date 2017/09/03
 */
public class DirectServiceInstanceSelector implements ServiceInstanceSelector {

    @Override
    public Optional<ServiceInstance> select(ConsumerDescriptor desc) {
        if (desc.isDirect()) {
            ServiceInstance inst = ServiceInstance.builder()
                .host(desc.getRemoteHost())
                .port(desc.getRemotePort())
                .serviceInterface(desc.getServiceInterface().getName())
                .serviceVersion(desc.getServiceVersion())
                .build();
            return Optional.of(inst);
        }
        return Optional.empty();
    }

    @Override
    public void reportUnavailable(ServiceInstance serviceInstance) {

    }
}
