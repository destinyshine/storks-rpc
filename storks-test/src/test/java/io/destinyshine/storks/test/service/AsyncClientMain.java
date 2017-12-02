package io.destinyshine.storks.test.service;

import java.util.Random;
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
import io.destinyshine.storks.test.service.api.ComputeService;
import io.destinyshine.storks.support.cnosume.NettyNioServiceReferenceSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author destinyliu
 */
public class AsyncClientMain {

    private static final Logger logger = LoggerFactory.getLogger(AsyncClientMain.class);

    private static final Random random = new Random();

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

        ConsumerDescriptor<ComputeService> desc = ConsumerBuilder
            .ofServiceInterface(ComputeService.class)
            .serviceVersion("1.0.0")
            .build();

        ConsumerProxyFactory consumerProxyFactory = new DefaultConsumerProxyFactory(invoker);

        ComputeService computeServiceConsumer = consumerProxyFactory.getConsumerProxy(desc);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    int a = random.nextInt(Integer.MAX_VALUE / 2);
                    int b = random.nextInt(Integer.MAX_VALUE / 2);
                    InvocationContext.doAsyncInvoke(() -> computeServiceConsumer.add(a, b))
                        .thenApply(result -> (int)result)
                        .thenAccept(sum -> logger.info("remote compute: {} + {} = {}, right:{}", a, b, sum, a + b == sum));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                invoker.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }));

        executorService.shutdown();
    }
}
