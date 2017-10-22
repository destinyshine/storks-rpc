package io.destinyshine.storks.registry.consul.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.destinyshine.storks.registry.consul.ConsulResponse;
import io.destinyshine.storks.registry.consul.ConsulService;

/**
 * Created by liujianyu.ljy on 17/9/13.
 *
 * @author liujianyu.ljy
 * @date 2017/09/13
 */
public interface ConsulClient {
    /**
     * 对指定checkid设置为pass状态
     *
     * @param serviceId
     */
    CompletableFuture<Void> checkPassService(String serviceId);

    /**
     * 设置checkid为不可用状态。
     *
     * @param serviceId
     */
    CompletableFuture<Void> checkFailService(String serviceId);

    /**
     * 注册一个consul service
     *
     * @param service
     */
    CompletableFuture<Void> registerService(ConsulService service);

    /**
     * 根据serviceid注销service
     *
     * @param serviceId
     */
    CompletableFuture<Void> unregisterService(String serviceId);

    /**
     * 获取最新的可用服务列表。
     *
     * @param serviceName
     * @param lastConsulIndex
     * @return
     */
    CompletableFuture<ConsulResponse<List<ConsulService>>> lookupHealthService(String serviceName, long lastConsulIndex);
}
