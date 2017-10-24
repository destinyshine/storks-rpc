package io.destinyshine.storks.core.provide;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;
import io.destinyshine.storks.utils.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liujianyu
 */
@Slf4j
public class ThreadPoolServiceProcedureExecutor implements ServiceProcedureExecutor {

    private ExecutorService procedureExecutor = new ThreadPoolExecutor(
        50,
        50,
        0,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(),
        new ThreadFactoryBuilder().setNameFormat("service-procedure-exec-%s").build()
    );

    public ThreadPoolServiceProcedureExecutor() {
    }

    @Override
    public CompletableFuture<ResponseMessage> execute(RequestMessage request,
                                                      ServiceProvider<?> serviceProvider) {
        CompletableFuture<ResponseMessage> completableFuture = new CompletableFuture<ResponseMessage>();
        procedureExecutor.submit(() ->
            doExecuteInternal(request, serviceProvider, completableFuture)
        );

        return completableFuture;
    }

    public void doExecuteInternal(RequestMessage request,
                                  ServiceProvider<?> serviceProvider,
                                  CompletableFuture<ResponseMessage> completableFuture) {

        Object provider = serviceProvider.getServiceObject();
        Class<?>[] parameterClasses = new Class<?>[request.getParameterTypes().length];
        for (int i = 0; i < request.getParameterTypes().length; i++) {
            try {
                parameterClasses[i] = Class.forName(request.getParameterTypes()[i]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        Throwable returnException = null;
        try {
            Method method = provider.getClass().getMethod(request.getMethodName(), parameterClasses);
            Object returnValue = method.invoke(provider, request.getParameters());
            ResponseMessage responseMessage = ResponseMessage.builder()
                .traceId(request.getTraceId())
                .returnValue(returnValue)
                .build();
            completableFuture.complete(responseMessage);
        } catch (NoSuchMethodException e) {
            returnException = e;
            logger.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            returnException = e;
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            returnException = e;
            logger.error(e.getMessage(), e);
        }

        if (Objects.nonNull(returnException)) {
            ResponseMessage responseMessage = ResponseMessage.builder()
                .traceId(request.getTraceId())
                .exception(returnException)
                .build();
            completableFuture.complete(responseMessage);
        }
    }
}