package io.destinyshine.storks.support.provide;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;
import io.destinyshine.storks.core.ServiceKey;
import io.destinyshine.storks.core.provide.ServiceProcedureExecutor;
import io.destinyshine.storks.core.provide.ServiceProvider;
import io.destinyshine.storks.core.provide.ThreadPoolServiceProcedureExecutor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by liujianyu.ljy on 17/8/10.
 *
 * @author liujianyu
 * @date 2017/08/10
 */
public class RequestMessageHandler extends SimpleChannelInboundHandler<RequestMessage> {

    private final Map<ServiceKey, ServiceProvider<?>> serviceProviders;
    private ServiceProcedureExecutor procedureExecutor = new ThreadPoolServiceProcedureExecutor();

    public RequestMessageHandler(Map<ServiceKey, ServiceProvider<?>> serviceProviders) {
        super(RequestMessage.class);
        this.serviceProviders = serviceProviders;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
        ServiceProvider<?> provider = serviceProviders.get(ServiceKey.of(msg.getServiceInterface(), msg.getServiceVersion()));
        CompletionStage<ResponseMessage> responseMessage = procedureExecutor.execute(msg, provider);
        responseMessage.thenAccept(ctx::writeAndFlush);
    }
}
