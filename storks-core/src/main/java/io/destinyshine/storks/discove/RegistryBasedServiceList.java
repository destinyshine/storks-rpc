package io.destinyshine.storks.discove;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import io.destinyshine.storks.core.ServiceInstance;
import io.destinyshine.storks.core.ServiceKey;
import io.destinyshine.storks.core.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by liujianyu.ljy on 17/9/10.
 *
 * @author liujianyu
 * @date 2017/09/10
 */
@Slf4j
public class RegistryBasedServiceList implements ServiceInstanceList {

    private final Set<ServiceKey> watchedServiceKeys = new HashSet<>();

    private final Map<ServiceKey, List<ServiceInstance>> serviceListCache = new ConcurrentHashMap<>();

    private final ServiceRegistry serviceRegistry;

    public RegistryBasedServiceList(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public List<ServiceInstance> getServiceList(ServiceKey serviceKey) {
        if (watchedServiceKeys.contains(serviceKey)) {
            return serviceListCache.get(serviceKey);
        } else {
            try {
                CompletionStage<List<ServiceInstance>> serviceListFuture = serviceRegistry.discover(serviceKey);
                List<ServiceInstance> serviceList = serviceListFuture.toCompletableFuture().get();

                serviceListCache.put(serviceKey, serviceList);

                //watch
                serviceRegistry.subscribe(serviceKey, (serviceListNew) -> {
                    serviceListCache.put(serviceKey, serviceListNew);
                });
                watchedServiceKeys.add(serviceKey);
                return serviceList;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public boolean isInitialized() {
        return true;
    }
}
