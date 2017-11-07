package io.destinyshine.storks.core.consume;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;
import io.destinyshine.storks.core.consume.invoke.InvocationContext;
import io.destinyshine.storks.core.consume.invoke.RemoteProcedureInvoker;

/**
 * Created by liujianyu.ljy on 17/9/2.
 *
 * @author liujianyu.ljy
 * @date 2017/09/02
 */
public class DefaultConsumerProxyFactory implements ConsumerProxyFactory {

    private final RemoteProcedureInvoker remoteProcedureInvoker;

    public DefaultConsumerProxyFactory(RemoteProcedureInvoker remoteProcedureInvoker) {
        this.remoteProcedureInvoker = remoteProcedureInvoker;
    }

    @Override
    public <T> T getConsumerProxy(ConsumerDescriptor<T> desc) {
        return createConsumerProxy(desc);
    }

    protected <T> T createConsumerProxy(ConsumerDescriptor<T> desc) {
        return (T)Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] {desc.getServiceInterface()},
            new ProxyingConsumerInvocationHandler(desc)
        );
    }

    private class ProxyingConsumerInvocationHandler implements InvocationHandler {

        private final ConsumerDescriptor<?> desc;

        public ProxyingConsumerInvocationHandler(ConsumerDescriptor<?> desc) {
            this.desc = desc;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            RequestMessage requestMessage = new RequestMessage();
            requestMessage.setMethodName(method.getName());
            Class<?>[] parameterTypes = method.getParameterTypes();
            String[] parameterTypeNames = new String[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterTypeNames[i] = parameterTypes[i].getName();
            }
            requestMessage.setParameterTypes(parameterTypeNames);
            requestMessage.setParameters(args);
            requestMessage.setServiceInterface(desc.getServiceInterface().getName());
            requestMessage.setServiceVersion(desc.getServiceVersion());
            Object returnValue = remoteProcedureInvoker.invoke(requestMessage, desc);
            return returnValue;
        }
    }
}
