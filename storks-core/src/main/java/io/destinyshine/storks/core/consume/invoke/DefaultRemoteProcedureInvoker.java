package io.destinyshine.storks.core.consume.invoke;

import java.util.concurrent.Future;

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
    public ResponseMessage invoke(ConsumerDescriptor desc, RequestMessage requestMessage) throws Exception {
        ServiceReference refer = getServiceRefer(desc);
        Future<ResponseMessage> responsePromise = refer.invoke(requestMessage);
        return responsePromise.get();
    }

    @Override
    public void close() throws Exception {
        //this.consumingContextManager.shutdown();
    }

}
