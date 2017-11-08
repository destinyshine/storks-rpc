package io.destinyshine.storks.sample.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.destinyshine.storks.core.consume.ConsumerBuilder;
import io.destinyshine.storks.core.consume.ConsumerDescriptor;
import io.destinyshine.storks.core.consume.ConsumerProxyFactory;
import io.destinyshine.storks.core.consume.DefaultConsumerProxyFactory;
import io.destinyshine.storks.core.consume.RandomLoadBalanceStrategy;
import io.destinyshine.storks.core.consume.invoke.DefaultRemoteProcedureInvoker;
import io.destinyshine.storks.core.consume.invoke.InvocationContext;
import io.destinyshine.storks.discove.DynamicListServiceInstanceSelector;
import io.destinyshine.storks.discove.RegistryBasedServiceList;
import io.destinyshine.storks.registry.consul.ConsulRegistry;
import io.destinyshine.storks.registry.consul.client.rest.RestConsulClient;
import io.destinyshine.storks.sample.service.api.HelloService;
import io.destinyshine.storks.support.cnosume.NettyNioServiceReferenceSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author destinyliu
 */
public class AsyncClientMain {

    private static final Logger logger = LoggerFactory.getLogger(AsyncClientMain.class);

    public static void main(String[] args) throws Exception {
        ConsulRegistry registry = new ConsulRegistry(new RestConsulClient("127.0.0.1", 8500));

        DefaultRemoteProcedureInvoker invoker = new DefaultRemoteProcedureInvoker();
        invoker.setServiceReferenceSupplier(new NettyNioServiceReferenceSupplier());
        invoker.setServiceInstanceSelector(
            new DynamicListServiceInstanceSelector(
                new RegistryBasedServiceList(registry),
                new RandomLoadBalanceStrategy()
            )
        );

        ConsumerDescriptor<HelloService> desc = ConsumerBuilder
            .ofServiceInterface(HelloService.class)
            .serviceVersion("1.0.0")
            .build();

        ConsumerProxyFactory consumerProxyFactory = new DefaultConsumerProxyFactory(invoker);

        HelloService helloServiceConsumer = consumerProxyFactory.getConsumerProxy(desc);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    String input = "tom-" + Thread.currentThread().getName() + "," + finalI;
                    InvocationContext.forAsync(() -> helloServiceConsumer.hello(input))
                        .thenAccept(result -> logger.info("input={}, get result: {}", input, result));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                ;
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                invoker.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }));

        //System.exit(0);
    }
}
