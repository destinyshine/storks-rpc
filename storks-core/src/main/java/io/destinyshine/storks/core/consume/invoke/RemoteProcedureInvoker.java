package io.destinyshine.storks.core.consume.invoke;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.consume.ConsumerDescriptor;

/**
 * Created by liujianyu.ljy on 17/9/2.
 *
 * @author liujianyu.ljy
 * @date 2017/09/02
 */
public interface RemoteProcedureInvoker {

    /**
     * invoke remote procedure
     *
     * @param desc
     * @param requestMessage
     * @return
     * @throws Exception
     */
    Object invoke(RequestMessage requestMessage, ConsumerDescriptor<?> desc)
        throws Exception;
}
