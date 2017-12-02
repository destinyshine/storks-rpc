package io.destinyshine.storks.test.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.destinyshine.storks.core.consume.ConsumerBuilder;
import io.destinyshine.storks.core.consume.ConsumerDescriptor;
import io.destinyshine.storks.core.consume.ConsumerProxyFactory;
import io.destinyshine.storks.core.consume.DefaultConsumerProxyFactory;
import io.destinyshine.storks.core.consume.DirectServiceInstanceSelector;
import io.destinyshine.storks.core.consume.invoke.DefaultRemoteProcedureInvoker;
import io.destinyshine.storks.test.service.api.HelloService;
import io.destinyshine.storks.support.cnosume.NettyNioServiceReferenceSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liujianyu
 */
public class DirectClientMain {

    private static final Logger logger = LoggerFactory.getLogger(DirectClientMain.class);

    public static void main(String[] args) throws Exception {

        DefaultRemoteProcedureInvoker invoker = new DefaultRemoteProcedureInvoker();
        invoker.setServiceReferenceSupplier(new NettyNioServiceReferenceSupplier());
        invoker.setServiceInstanceSelector(new DirectServiceInstanceSelector());

        ConsumerDescriptor<HelloService> desc = ConsumerBuilder
            .ofServiceInterface(HelloService.class)
            .remoteServer("127.0.0.1")
            .remotePort(39874)
            .serviceVersion("1.0.0")
            .direct(true)
            .build();

        ConsumerProxyFactory consumerProxyFactory = new DefaultConsumerProxyFactory(invoker);

        HelloService helloServiceConsumer = consumerProxyFactory.getConsumerProxy(desc);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executorService.submit(() -> {
                String input = null;
                String result = null;
                try {
                    input = "tom,direct," + Thread.currentThread().getName() + "," + finalI;
                    result = helloServiceConsumer.hello(input);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                logger.info("input={}, get result: {}", input, result);
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
