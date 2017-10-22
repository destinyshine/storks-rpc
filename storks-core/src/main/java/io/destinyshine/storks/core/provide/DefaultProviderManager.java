package io.destinyshine.storks.core.provide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.destinyshine.storks.core.ServiceKey;
import io.destinyshine.storks.core.StorksApplication;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toList;

/**
 * 管理所有的Provider，Provider以{@link ProviderDescriptor}类来表示，所有的Provider需要注册到ProviderManager中。
 * <p>
 *     ServiceExporter只是负责搭建网络边界的沟通桥梁，而具体的provider业务代码执行由ProviderManager负责。
 *     ServiceExporter负责将网络请求还原为request，以及将response发送到网络；而providerManager负责查找本地Provider和执行request。
 * <p/>
 * <p>
 *     根据以上设计，如果本地ProviderTable没有指定的Provider，则这个NoProviderDefinedException异常由ProviderManager抛出，而不是ServiceExporter。
 *
 * </p>
 * @author liujianyu
 */
@Slf4j
public class DefaultProviderManager implements ProviderManager {

    private Map<ServiceKey, List<ServiceProvider<?>>> localProviderTable = new ConcurrentHashMap<ServiceKey, List<ServiceProvider<?>>>();

    private StorksApplication application;

    private List<ServiceProviderListener> serviceProviderListeners = new ArrayList<>();

    private final List<ServiceExporter> serviceExporters;

    public DefaultProviderManager(List<ServiceExporter> serviceExporters) {
        this.serviceExporters = new ArrayList<>(serviceExporters);
    }

    public DefaultProviderManager(ServiceExporter... serviceExporters) {
        this.serviceExporters = new ArrayList<>(Arrays.asList(serviceExporters));
    }

    @Override
    public <T> void addProvider(ProviderDescriptor<T> desc) {
        ServiceKey serviceKey = ServiceKey.of(desc);

        List<ServiceProvider<?>> providers = serviceExporters.stream()
            .filter(exporter -> exporter.support(desc))
            .map(exporter -> exporter.export(desc))
            .collect(toList());

        localProviderTable.put(serviceKey, providers);

        providers.forEach(this::fireProviderAdd);
    }

    @Override
    public <T> void removeProvider(ProviderDescriptor<T> desc) {
        ServiceKey serviceKey = ServiceKey.of(desc);

        List<ServiceProvider<?>> providers = localProviderTable.remove(serviceKey);

        serviceExporters.stream()
            .filter(exporter -> exporter.support(desc))
            .forEach(exporter -> exporter.remove(desc));

        providers.forEach(this::fireProviderRemove);
    }

    protected void fireProviderAdd(ServiceProvider<?> provider) {
        for (ServiceProviderListener listener : serviceProviderListeners) {
            listener.onServiceProviderAdded(provider);
        }
    }

    protected void fireProviderRemove(ServiceProvider<?> provider) {
        for (ServiceProviderListener listener : serviceProviderListeners) {
            listener.onServiceProviderRemoved(provider);
        }
    }

    @Override
    public void addServiceProviderListener(ServiceProviderListener listener) {
        this.serviceProviderListeners.add(listener);
    }

    @Override
    public void removeServiceProviderListener(ServiceProviderListener listener) {
        this.serviceProviderListeners.remove(listener);
    }

    @Override
    public StorksApplication getApplication() {
        return application;
    }

    public void setApplication(StorksApplication application) {
        this.application = application;
    }

    @Override
    public Map<ServiceKey, List<ServiceProvider<?>>> getProviders() {
        return localProviderTable;
    }
}
