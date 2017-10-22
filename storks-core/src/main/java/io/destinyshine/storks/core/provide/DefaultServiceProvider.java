package io.destinyshine.storks.core.provide;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liujianyu
 * @date 2017/09/16
 */
@Slf4j
@Data
@Builder
public class DefaultServiceProvider<T> implements ServiceProvider<T> {

    private final String protocol;

    private final String serviceHost;

    private final int servicePort;

    protected final Class<T> serviceInterface;

    protected final String serviceVersion;

    private final T serviceObject;

    @Override
    public ResponseMessage execute(RequestMessage request) {
        T provider = this.getServiceObject();
        Class<?>[] parameterClasses = new Class<?>[request.getParameterTypes().length];
        for (int i = 0; i < request.getParameterTypes().length; i++) {
            try {
                parameterClasses[i] = Class.forName(request.getParameterTypes()[i]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            Method method = provider.getClass().getMethod(request.getMethodName(), parameterClasses);
            Object returnValue = method.invoke(provider, request.getParameters());
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setReturnValue(returnValue);
            responseMessage.setTraceId(request.getTraceId());
            return responseMessage;
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

}
