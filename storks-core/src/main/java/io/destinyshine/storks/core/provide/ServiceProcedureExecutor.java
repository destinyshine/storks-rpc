package io.destinyshine.storks.core.provide;

import java.util.concurrent.CompletableFuture;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;

/**
 * @author liujianyu
 */
public interface ServiceProcedureExecutor {

    /**
     *
     * @param request request from remote client
     * @param serviceProvider witch serviceProvider will invoke
     * @return CompletableFuture<ResponseMessage>
     */
    CompletableFuture<ResponseMessage> execute(RequestMessage request,
                                               ServiceProvider<?> serviceProvider);
}
