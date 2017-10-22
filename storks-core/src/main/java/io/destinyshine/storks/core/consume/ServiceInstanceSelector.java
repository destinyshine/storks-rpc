package io.destinyshine.storks.core.consume;

import java.util.Optional;

import io.destinyshine.storks.core.ServiceInstance;

/**
 * Created by liujianyu.ljy on 17/8/20.
 *
 * @author liujianyu.ljy
 * @date 2017/08/20
 */
public interface ServiceInstanceSelector {

    /**
     * get a available service instance
     *
     * @param desc
     * @return
     */
    Optional<ServiceInstance> select(ConsumerDescriptor desc);

    void reportUnavailable(ServiceInstance serviceInstance);
}
