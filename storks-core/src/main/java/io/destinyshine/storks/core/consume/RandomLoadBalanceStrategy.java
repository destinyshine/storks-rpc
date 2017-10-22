package io.destinyshine.storks.core.consume;

import java.util.List;
import java.util.Random;

import io.destinyshine.storks.core.ServiceInstance;
import io.destinyshine.storks.discove.LoadBalanceStrategy;

/**
 * Created by liujianyu.ljy on 17/9/2.
 *
 * @author liujianyu.ljy
 * @date 2017/09/02
 */
public class RandomLoadBalanceStrategy implements LoadBalanceStrategy {

    private Random random = new Random();

    @Override
    public ServiceInstance select(List<ServiceInstance> instances, ConsumerDescriptor desc) {
        if (instances == null || instances.size() == 0) {
            throw new IllegalArgumentException("instances can not be empty.");
        } else {
            int index = random.nextInt(instances.size());
            ServiceInstance inst = instances.get(index);
            return inst;
        }
    }
}
