package io.destinyshine.storks.core;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import io.destinyshine.storks.core.consume.Subscription;

/**
 * Created by liujianyu.ljy on 17/8/18.
 *
 * @author liujianyu
 * @date 2017/08/18
 */
public interface ServiceRegistry extends AutoCloseable {

    /**
     * register service
     *
     * @param instance
     */
    void register(ServiceInstance instance);

    /**
     * unregister service
     *
     * @param instance
     */
    void unregister(ServiceInstance instance);

    /**
     * discover service
     *
     * @param serviceKey
     * @return
     */
    CompletionStage<List<ServiceInstance>> discover(ServiceKey serviceKey);

    /**
     * watching long time unit cancel. may trigger many times.
     *
     * @param serviceKey serviceKey subscribed.
     * @param callback   callback function.
     * @return
     */
    Subscription<List<ServiceInstance>> subscribe(ServiceKey serviceKey, Consumer<List<ServiceInstance>> callback);

    void unsubscribe(Subscription<?> subscription);
}
