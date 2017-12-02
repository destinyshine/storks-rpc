package io.destinyshine.storks.discove;

import java.util.List;

import io.destinyshine.storks.core.ServiceInstance;
import io.destinyshine.storks.core.consume.ConsumerDescriptor;

/**
 * Created by liujianyu.ljy on 17/9/2.
 *
 * @author liujianyu
 * @date 2017/09/02
 */
public interface LoadBalanceStrategy {

    ServiceInstance select(List<ServiceInstance> instances,
                           ConsumerDescriptor desc);

}
