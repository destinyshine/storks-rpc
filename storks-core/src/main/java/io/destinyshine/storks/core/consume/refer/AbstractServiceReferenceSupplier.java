package io.destinyshine.storks.core.consume.refer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.destinyshine.storks.core.ServiceInstance;
import lombok.extern.slf4j.Slf4j;

/**
 * @author destinyliu
 */
@Slf4j
public abstract class AbstractServiceReferenceSupplier implements ServiceReferenceSupplier {

    private Map<ServiceInstance, ServiceReference> connectionCache = new ConcurrentHashMap<>();

    @Override
    public synchronized ServiceReference getServiceReference(ServiceInstance instance) throws Exception {
        ServiceReference con = connectionCache.computeIfAbsent(instance,
            instance1 -> {
                try {
                    return createServiceReferInternal(instance1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });

        return con;
    }

    /**
     * create connection
     *
     * @param instance
     * @return
     * @throws Exception
     */
    protected abstract ServiceReference createServiceReferInternal(ServiceInstance instance) throws Exception;

    public void shutdown() {
        logger.warn("shutting down connectionManager...");
        connectionCache.forEach((serviceKey, con) -> {
            logger.warn("closing all connections of serviceKey={}", serviceKey);
            try {
                con.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            logger.warn(
                "closed connection of serviceKey={}, {}",
                serviceKey,
                con
            );
            logger.warn("closed all connections of serviceKey={}", serviceKey);
        });
        logger.warn("shutdown connectionManager finished.");
    }
}
