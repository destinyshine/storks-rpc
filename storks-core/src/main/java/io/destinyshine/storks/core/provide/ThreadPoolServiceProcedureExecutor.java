package io.destinyshine.storks.core.provide;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;
import io.destinyshine.storks.lang.PrimitiveTypes;
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
    public CompletionStage<ResponseMessage> execute(RequestMessage request,
                                                    ServiceProvider<?> serviceProvider) {
        CompletableFuture<ResponseMessage> completableFuture = new CompletableFuture<ResponseMessage>();
        procedureExecutor.submit(() ->
            doExecuteInternal(request, serviceProvider, completableFuture)
        );

        return completableFuture;
    }

    private void doExecuteInternal(RequestMessage request,
                                  ServiceProvider<?> serviceProvider,
                                  CompletableFuture<ResponseMessage> completableFuture) {

        Object provider = serviceProvider.getServiceObject();
        Class<?>[] parameterClasses = new Class<?>[request.getParameterTypes().length];
        for (int i = 0; i < request.getParameterTypes().length; i++) {
            try {
                String paramTypeName = request.getParameterTypes()[i];
                Optional<Class<?>> primitiveType = PrimitiveTypes.getPrimitiveType(paramTypeName);
                parameterClasses[i] = primitiveType.isPresent() ? primitiveType.get() : Class.forName(paramTypeName);
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