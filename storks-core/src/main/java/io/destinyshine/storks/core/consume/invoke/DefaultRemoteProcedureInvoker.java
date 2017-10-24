package io.destinyshine.storks.core.consume.invoke;

import java.util.concurrent.CompletionStage;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;
import io.destinyshine.storks.core.consume.ConsumerDescriptor;
import io.destinyshine.storks.core.consume.refer.ServiceReference;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liujianyu
 */
@Slf4j
public class DefaultRemoteProcedureInvoker extends RemoteServiceAccessor
    implements RemoteProcedureInvoker, AutoCloseable {

    @Override
    public CompletionStage<ResponseMessage> invoke(ConsumerDescriptor desc, RequestMessage requestMessage) throws Exception {
        ServiceReference refer = getServiceRefer(desc);
        CompletionStage<ResponseMessage> responsePromise = refer.invoke(requestMessage);
        return responsePromise;
    }

    @Override
    public void close() throws Exception {
        //this.consumingContextManager.shutdown();
    }

}
