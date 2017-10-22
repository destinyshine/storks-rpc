package io.destinyshine.storks.support.provide;

import java.util.Map;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;
import io.destinyshine.storks.core.ServiceKey;
import io.destinyshine.storks.core.provide.ServiceProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by liujianyu.ljy on 17/8/10.
 *
 * @author liujianyu.ljy
 * @date 2017/08/10
 */
public class RequestMessageHandler extends SimpleChannelInboundHandler<RequestMessage> {

    private Map<ServiceKey, ServiceProvider<?>> serviceProviders;

    public RequestMessageHandler(Map<ServiceKey, ServiceProvider<?>> serviceProviders) {
        super(RequestMessage.class);
        this.serviceProviders = serviceProviders;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage msg) throws Exception {
        ServiceProvider<?> provider = serviceProviders.get(ServiceKey.of(msg.getServiceInterface(), msg.getServiceVersion()));
        ResponseMessage responseMessage = provider.execute(msg);
        ctx.writeAndFlush(responseMessage);
    }
}
