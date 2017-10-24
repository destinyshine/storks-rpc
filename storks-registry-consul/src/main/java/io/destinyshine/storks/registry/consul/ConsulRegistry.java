package io.destinyshine.storks.registry.consul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.destinyshine.storks.core.ServiceInstance;
import io.destinyshine.storks.core.ServiceKey;
import io.destinyshine.storks.core.ServiceNotFoundException;
import io.destinyshine.storks.core.ServiceRegistry;
import io.destinyshine.storks.core.consume.Subscription;
import io.destinyshine.storks.registry.consul.client.ConsulClient;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toList;

/**
 * @author destinyliu
 */
@Slf4j
public class ConsulRegistry implements ServiceRegistry {

    /**
     * default healthCheckInterval in seconds
     */
    public static final int DEFAULT_HEALTH_CHECK_INTERVAL = 5 * 60;

    /**
     * healthCheckInterval in seconds
     */
    private final int healthCheckInterval;

    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);

    private final Map<String, ConsulService> registeredServices = new HashMap<>();

    private final ConsulClient client;

    private final Map<ServiceKey, Long> lastConsulIndexMap = new ConcurrentHashMap<>();

    private final Map<ServiceKey, List<Subscription<List<ServiceInstance>>>> allWatchers = new HashMap<>();

    private final int fetchIntervalSeconds;
    private final int reportIntervalSeconds;

    private final AtomicInteger lastSubscriptionIndex = new AtomicInteger();

    public ConsulRegistry(ConsulClient client) {
        this(client, DEFAULT_HEALTH_CHECK_INTERVAL);
    }

    public ConsulRegistry(ConsulClient client, int healthCheckInterval) {
        this.client = client;
        this.healthCheckInterval = healthCheckInterval;
        this.fetchIntervalSeconds = (int)(healthCheckInterval* 0.6);
        this.reportIntervalSeconds = (int)(healthCheckInterval* 0.6);
        this.startAutoFetch();
        this.startAutoPush();
    }

    private void startAutoPush() {
        executorService.scheduleWithFixedDelay(
            this::reportToRegistry,
            reportIntervalSeconds, reportIntervalSeconds, TimeUnit.SECONDS
        );
    }

    private void startAutoFetch() {
        executorService.scheduleWithFixedDelay(
            this::notifyWatchersIfUpdated,
            reportIntervalSeconds, fetchIntervalSeconds, TimeUnit.SECONDS
        );
    }

    private void reportToRegistry() {
        registeredServices.values()
            .stream()
            .map(ConsulService::getId)
            .forEach(client::checkPassService);
    }

    private void notifyWatchersIfUpdated() {
        allWatchers.forEach((key, watchers) ->
            discoverInternal(key, lastConsulIndexMap.get(key))
                .thenAccept(servicesOpt ->
                    servicesOpt.ifPresent(serviceList ->
                        watchers.forEach(subscription ->
                            subscription.getCallback().accept(serviceList)
                        )
                    )
                )
        );

    }

    @Override
    public void register(ServiceInstance instance) {
        ConsulService service = ConsulServiceHelper.toConsulService(instance, healthCheckInterval);
        logger.info("will register, service={}", instance);
        //register async
        client.registerService(service)
            .handle((e, result) -> {
                if (Objects.nonNull(e)) {
                    logger.error("error on register, service={}", service, e);
                } else {
                    logger.info("register success, service={}, will check pass", service);
                    client.checkPassService(service.getId())
                        .exceptionally(checkError -> {
                            logger.error("error on checkPass, service={}", service, checkError);
                            return null;
                        });
                    //add to local list.
                    registeredServices.put(service.getId(), service);
                }
                return null;
            });
    }

    @Override
    public void unregister(ServiceInstance instance) {
        String serviceId = ConsulServiceHelper.buildConsulServiceId(instance);
        //remove from local list.
        registeredServices.remove(serviceId);
        //checkFail async
        client.checkFailService(serviceId)
            .handle((e, result) -> {
                if (Objects.nonNull(e)) {
                    logger.error("error on checkFail, serviceId={}", serviceId, e);
                } else {
                    logger.info("checkFail success, serviceId={}, then do unregister", serviceId);
                    client.unregisterService(serviceId)
                        .exceptionally(checkError -> {
                            logger.error("error on unregister, serviceId={}", serviceId, checkError);
                            return null;
                        });
                }
                return null;
            });
    }

    @Override
    public CompletionStage<List<ServiceInstance>> discover(ServiceKey serviceKey) {
        CompletionStage<Optional<List<ServiceInstance>>> serviceListNew = discoverInternal(serviceKey, -1);
        return serviceListNew.thenApply(servicesOpt -> servicesOpt.get());
    }

    @Override
    public Subscription<List<ServiceInstance>> subscribe(ServiceKey serviceKey,
                                                         Consumer<List<ServiceInstance>> callback) {
        Subscription<List<ServiceInstance>> subscription = new Subscription<>(
            lastSubscriptionIndex.incrementAndGet(),
            serviceKey,
            callback
        );

        allWatchers.compute(serviceKey, (key, watchers) -> {
            if (watchers != null) {
                watchers.add(subscription);
                return watchers;
            }

            List<Subscription<List<ServiceInstance>>> newSubscriptions = new ArrayList<>();
            newSubscriptions.add(subscription);
            return newSubscriptions;
        });
        return subscription;
    }

    @Override
    public void unsubscribe(Subscription<?> subscription) {
        this.allWatchers.computeIfPresent(
            subscription.getServiceKey(),
            (key, subs) -> subs.stream()
                .filter(Predicate.isEqual(subscription).negate())
                .collect(toList())
        );
    }

    private CompletionStage<Optional<List<ServiceInstance>>> discoverInternal(ServiceKey serviceKey,
                                                                                long lastConsulIndexId) {
        CompletionStage<ConsulResponse<List<ConsulService>>> responseFuture;
        responseFuture = lookupConsulService(serviceKey, lastConsulIndexId);
        return responseFuture.thenApply(response -> {
            if (response == null) {
                throw new ServiceNotFoundException("response=null, can not found any service in registry.");
            }
            List<ConsulService> services = response.getValue();
            if (services != null && !services.isEmpty()) {

                if (response.getConsulIndex() == lastConsulIndexId) {
                    return Optional.empty();
                }

                lastConsulIndexMap.put(serviceKey, response.getConsulIndex());

                List<ServiceInstance> serviceInstances = response.getValue().stream()
                    .map(service -> ConsulServiceHelper.toServiceInstance(service))
                    .collect(toList());

                return Optional.of(serviceInstances);

            } else {
                throw new ServiceNotFoundException("can not found any service in registry.");
            }
        });

    }

    /**
     * directly fetch consul service data.
     *
     * @param serviceKey
     * @return ConsulResponse or null
     */
    private CompletionStage<ConsulResponse<List<ConsulService>>> lookupConsulService(ServiceKey serviceKey,
                                                                                       long lastConsulIndexId) {
        CompletionStage<ConsulResponse<List<ConsulService>>> response = client.lookupHealthService(
            serviceKey.toString(),
            lastConsulIndexId
        );
        return response;
    }

    @Override
    public void close() {

    }

}
