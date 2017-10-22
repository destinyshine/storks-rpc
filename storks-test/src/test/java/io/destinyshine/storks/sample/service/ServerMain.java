package io.destinyshine.storks.sample.service;

import io.destinyshine.storks.core.RegistryBasedExporter;
import io.destinyshine.storks.core.StorksApplication;
import io.destinyshine.storks.core.provide.DefaultProviderManager;
import io.destinyshine.storks.core.provide.ProviderDescriptor;
import io.destinyshine.storks.registry.consul.ConsulRegistry;
import io.destinyshine.storks.registry.consul.client.rest.RestConsulClient;
import io.destinyshine.storks.sample.service.api.HelloService;
import io.destinyshine.storks.sample.service.impl.HelloServiceImpl;
import io.destinyshine.storks.support.provide.NettyNioServiceExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liujianyu
 */
public class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) throws Exception {

        logger.info("--server main--");

        StorksApplication app = new StorksApplication("testProvider");

        ProviderDescriptor desc = new ProviderDescriptor(HelloService.class, "1.0.0", new HelloServiceImpl());

        NettyNioServiceExporter internalExporter = new NettyNioServiceExporter(app,0);
        ConsulRegistry registry = new ConsulRegistry(new RestConsulClient("127.0.0.1", 8500));
        RegistryBasedExporter exporter = new RegistryBasedExporter(registry, internalExporter, app);

        DefaultProviderManager providerManager = new DefaultProviderManager(exporter);
        providerManager.setApplication(app);

        //add provider
        providerManager.addProvider(desc);

        logger.info("exporter started.");

    }
}
