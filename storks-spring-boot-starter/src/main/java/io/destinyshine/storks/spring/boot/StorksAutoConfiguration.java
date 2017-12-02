package io.destinyshine.storks.spring.boot;

import io.destinyshine.storks.core.RegistryBasedExporter;
import io.destinyshine.storks.core.ServiceRegistry;
import io.destinyshine.storks.core.StorksApplication;
import io.destinyshine.storks.core.consume.ConsumerProxyFactory;
import io.destinyshine.storks.core.consume.DefaultConsumerProxyFactory;
import io.destinyshine.storks.core.consume.RandomLoadBalanceStrategy;
import io.destinyshine.storks.core.consume.invoke.DefaultRemoteProcedureInvoker;
import io.destinyshine.storks.core.consume.invoke.RemoteProcedureInvoker;
import io.destinyshine.storks.core.provide.DefaultProviderManager;
import io.destinyshine.storks.core.provide.ProviderDescriptor;
import io.destinyshine.storks.core.provide.ProviderManager;
import io.destinyshine.storks.core.provide.ServiceExporter;
import io.destinyshine.storks.discove.DynamicListServiceInstanceSelector;
import io.destinyshine.storks.discove.RegistryBasedServiceList;
import io.destinyshine.storks.registry.consul.ConsulRegistry;
import io.destinyshine.storks.registry.consul.client.ConsulClient;
import io.destinyshine.storks.registry.consul.client.rest.RestConsulClient;
import io.destinyshine.storks.spring.boot.StorksProperties.ConsulProperties;
import io.destinyshine.storks.support.cnosume.NettyNioServiceReferenceSupplier;
import io.destinyshine.storks.support.provide.NettyNioServiceExporter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author liujianyu.ljy
 */
@Configuration
@ConditionalOnClass({ProviderManager.class, RemoteProcedureInvoker.class})
@EnableConfigurationProperties({StorksProperties.class})
@Import(StorksServiceProviderRegistrar.class)
public class StorksAutoConfiguration {

    @Autowired
    private StorksProperties properties;

    @Value("${spring.application.name}")
    private String springAppName;

    @Bean
    public InstantiationAwareBeanPostProcessor consumerAnnotationPostProcessor() {
        return new ConsumerAnnotationPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(StorksApplication.class)
    public StorksApplication storksApplication() {

        String appName = properties.getAppName();
        if (StringUtils.isBlank(appName)) {
            appName = springAppName;
        }

        StorksApplication app = new StorksApplication(appName);
        return app;
    }

    @Bean
    @ConditionalOnMissingBean(ConsulClient.class)
    public RestConsulClient restConsulClient() {
        ConsulProperties consulProps = properties.getRegistry().getConsul();
        return new RestConsulClient(consulProps.getHost(), consulProps.getPort());
    }

    @Bean
    @ConditionalOnMissingBean(ConsulRegistry.class)
    public ConsulRegistry consulRegistry(RestConsulClient consulClient) {
        ConsulRegistry registry = new ConsulRegistry(consulClient);
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean(ServiceExporter.class)
    public RegistryBasedExporter registryBasedExporter(StorksApplication app) {
        NettyNioServiceExporter internalExporter = new NettyNioServiceExporter(app,
            properties.getProvider().getServicePort());
        ConsulProperties consulProps = properties.getRegistry().getConsul();
        ConsulRegistry registry = new ConsulRegistry(
            new RestConsulClient(consulProps.getHost(), consulProps.getPort()),
            consulProps.getHealthCheckIntervalSeconds()
        );
        RegistryBasedExporter exporter = new RegistryBasedExporter(registry, internalExporter, app);

        return exporter;
    }

    @Bean
    public ProviderDescriptor<Void> voidProviderDescriptorPlaceholder() {
        return new ProviderDescriptor<>(Void.class, "0.0.0", null);
    }

    @Bean
    @ConditionalOnMissingBean(ProviderManager.class)
    public DefaultProviderManager defaultProviderManager(StorksApplication app,
                                           ServiceExporter exporter,
                                           ProviderDescriptor<?>[] providerDescriptors) {
        DefaultProviderManager providerManager = new DefaultProviderManager(exporter);
        providerManager.setApplication(app);
        for (ProviderDescriptor desc : providerDescriptors) {
            if (Void.class.equals(desc.getServiceInterface())) {
                continue;
            }
            providerManager.addProvider(desc);
        }
        return providerManager;
    }

    @Bean
    @ConditionalOnMissingBean(RemoteProcedureInvoker.class)
    public DefaultRemoteProcedureInvoker defaultRemoteProcedureInvoker(ServiceRegistry registry) {
        DefaultRemoteProcedureInvoker invoker = new DefaultRemoteProcedureInvoker();
        invoker.setServiceReferenceSupplier(new NettyNioServiceReferenceSupplier());
        invoker.setServiceInstanceSelector(
            new DynamicListServiceInstanceSelector(
                new RegistryBasedServiceList(registry),
                new RandomLoadBalanceStrategy()
            )
        );
        return invoker;
    }

    @Bean
    @ConditionalOnMissingBean(ConsumerProxyFactory.class)
    public ConsumerProxyFactory consumerProxyFactory(RemoteProcedureInvoker invoker) {
        ConsumerProxyFactory consumerProxyFactory = new DefaultConsumerProxyFactory(invoker);
        return consumerProxyFactory;
    }

}
