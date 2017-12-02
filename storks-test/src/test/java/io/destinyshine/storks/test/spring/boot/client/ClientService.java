package io.destinyshine.storks.test.spring.boot.client;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.destinyshine.storks.core.consume.invoke.InvocationContext;
import io.destinyshine.storks.test.service.api.ComputeService;
import io.destinyshine.storks.spring.boot.StorksConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClientService implements InitializingBean {

    private static final Random random  = new Random();

    @StorksConsumer(serviceVersion = "1.0.0")
    private ComputeService computeService;

    @Override
    public void afterPropertiesSet() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    int a = random.nextInt(Integer.MAX_VALUE / 2);
                    int b = random.nextInt(Integer.MAX_VALUE / 2);
                    InvocationContext.doAsyncInvoke(() -> computeService.add(a, b))
                        .thenApply(result -> (int)result)
                        .thenAccept(
                            sum -> logger.info("remote compute: {} + {} = {}, right:{}", a, b, sum, a + b == sum));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });
        }
    }
}
