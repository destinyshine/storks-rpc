package io.destinyshine.storks.core.consume.invoke;

import java.util.concurrent.CompletionStage;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;
import io.destinyshine.storks.core.consume.ConsumerDescriptor;
import io.destinyshine.storks.core.consume.refer.ServiceReference;
import io.destinyshine.storks.lang.PrimitiveTypes;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liujianyu
 */
@Slf4j
public class DefaultRemoteProcedureInvoker extends RemoteServiceAccessor
    implements RemoteProcedureInvoker, AutoCloseable {

    @Override
    public Object invoke(RequestMessage requestMessage, ConsumerDescriptor<?> desc) throws Exception {
        ServiceReference refer = getServiceRefer(desc);
        CompletionStage<ResponseMessage> responsePromise = refer.invoke(requestMessage);

        if (InvocationContext.getContext().isAsyncResultMode()) {
            CompletionStage<Object> promise = responsePromise.thenApply(resp -> resp.getReturnValue());
            InvocationContext.getContext().pushPromise(promise);
            Class<?> returnType = requestMessage.getReturnType();
            if (returnType.isPrimitive()) {
                return PrimitiveTypes.getPrimitiveDefaultValue(returnType);
            }
            return null;
        }
        return responsePromise.toCompletableFuture().get().getReturnValue();
    }

    @Override
    public void close() throws Exception {
        //this.consumingContextManager.shutdown();
    }

}
