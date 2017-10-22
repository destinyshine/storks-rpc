package io.destinyshine.storks.core;

import java.util.Objects;

import io.destinyshine.storks.core.provide.ProviderDescriptor;
import io.destinyshine.storks.core.provide.ServiceExporter;
import io.destinyshine.storks.core.provide.ServiceProvider;
import io.destinyshine.storks.utils.InetAddressUtils;

/**
 * @author liujianyu
 */
public class RegistryBasedExporter implements ServiceExporter {

    private final ServiceRegistry registry;

    private final ServiceExporter delegate;

    private final StorksApplication application;

    public RegistryBasedExporter(ServiceRegistry registry,
                                 ServiceExporter delegate,
                                 StorksApplication application) {
        Objects.requireNonNull(registry, "registry cannot be null.");
        Objects.requireNonNull(delegate, "delegate cannot be null.");
        Objects.requireNonNull(application, "application cannot be null.");
        this.registry = registry;
        this.delegate = delegate;
        this.application = application;
    }

    @Override
    public <T> boolean support(final ProviderDescriptor<T> desc) {
        return delegate.support(desc);
    }

    @Override
    public <T> ServiceProvider<T> export(ProviderDescriptor<T> desc) {
        ServiceProvider<T> provider = delegate.export(desc);
        registry.register(providerToInstance(provider));
        return provider;
    }

    @Override
    public <T> void remove(ProviderDescriptor<T> desc) {
        delegate.remove(desc);
    }

    private <T> ServiceInstance providerToInstance(ServiceProvider<T> provider) {
        ServiceInstance serviceInstance = ServiceInstance.builder()
            .appName(application.getAppName())
            .protocol("storks")
            .host(InetAddressUtils.getLocalAddress().getHostAddress())
            .port(provider.getServicePort())
            .serviceInterface(provider.getServiceInterface().getName())
            .serviceVersion(provider.getServiceVersion())
            .build();
        return serviceInstance;
    }
}
