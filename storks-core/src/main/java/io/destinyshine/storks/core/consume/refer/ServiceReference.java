package io.destinyshine.storks.core.consume.refer;

import java.util.concurrent.CompletionStage;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;

/**
 * 消费者持有连接
 * <p>
 * 指向一个远程服务端，此类是实际上的Client
 * </p>
 *
 * @author liujianyu.ljy
 * @date 2017/08/27
 */
public interface ServiceReference extends AutoCloseable {

    /**
     * connect to remote service.
     *
     * @throws Exception
     */
    void connect() throws Exception;

    /**
     * send a request to remote service and wait for response.
     *
     * @param requestMessage
     * @return
     */
    CompletionStage<ResponseMessage> invoke(RequestMessage requestMessage);

}
