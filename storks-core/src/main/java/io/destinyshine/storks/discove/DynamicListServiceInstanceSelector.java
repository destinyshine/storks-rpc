package io.destinyshine.storks.discove;

import java.util.List;
import java.util.Optional;

import io.destinyshine.storks.core.ServiceInstance;
import io.destinyshine.storks.core.ServiceKey;
import io.destinyshine.storks.core.consume.ConsumerDescriptor;
import io.destinyshine.storks.core.consume.ServiceInstanceSelector;

/**
 * Created by liujianyu.ljy on 17/8/20.
 *
 * @author liujianyu
 * @date 2017/08/20
 */
public class DynamicListServiceInstanceSelector implements ServiceInstanceSelector {

    private ServiceInstanceList serviceInstanceList;

    private LoadBalanceStrategy loadBalanceStrategy;

    public DynamicListServiceInstanceSelector(ServiceInstanceList serviceInstanceList,
                                              LoadBalanceStrategy loadBalanceStrategy) {
        this.serviceInstanceList = serviceInstanceList;
        this.loadBalanceStrategy = loadBalanceStrategy;
    }

    @Override
    public Optional<ServiceInstance> select(ConsumerDescriptor desc) {

        List<ServiceInstance> instances = serviceInstanceList.getServiceList((ServiceKey.of(desc)));
        ServiceInstance instance = loadBalanceStrategy.select(instances, desc);
        return Optional.of(instance);
    }

    @Override
    public void reportUnavailable(ServiceInstance serviceInstance) {

    }

}
