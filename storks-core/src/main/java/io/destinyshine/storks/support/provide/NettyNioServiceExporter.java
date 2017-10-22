package io.destinyshine.storks.support.provide; /**
 * 基于Netty NIO实现的服务发布。
 * <p>
 * 将服务发布到网络环境，可以供消费端调用，处理消费端请求并返回响应消息。
 * </p>
 *
 * @author liujianyu
 * @date 2017/08/10
 */

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;
import io.destinyshine.storks.core.ServiceKey;
import io.destinyshine.storks.core.StorksApplication;
import io.destinyshine.storks.core.provide.DefaultServiceProvider;
import io.destinyshine.storks.core.provide.ProviderDescriptor;
import io.destinyshine.storks.core.provide.ServiceExporter;
import io.destinyshine.storks.core.provide.ServiceProvider;
import io.destinyshine.storks.support.ProtostuffDecoder;
import io.destinyshine.storks.support.ProtostuffEncoder;
import io.destinyshine.storks.support.StorksProtocolHelper;
import io.destinyshine.storks.utils.InetAddressUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyNioServiceExporter implements ServiceExporter {

    private final StorksApplication application;

    private final int servicePort;

    private final String supportedProtocol;

    private Channel channel;
    private InetSocketAddress localAddress;
    private boolean working;

    private Map<ServiceKey, ServiceProvider<?>> serviceProviders = new HashMap<>();

    /**
     * 使用一个本地服务端口构造exporter，将通过指定端口提供服务。
     * <p>
     * 如果localPort为0，代表使用随机端口，在服务启动后通过{@link #getServicePort()}获取实际端口。
     * </p>
     *
     * @param servicePort 服务端口，为0可使用随机端口。
     * @see #getServicePort()
     */
    public NettyNioServiceExporter(StorksApplication application, int servicePort) {
        this.servicePort = servicePort;
        this.application = application;
        this.supportedProtocol = "storks";
        this.startup();
    }

    private void startup() {

        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap.group(group);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.localAddress(new InetSocketAddress(servicePort));
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(StorksProtocolHelper.newFrameDecoder())
                        .addLast(new ProtostuffDecoder<>(RequestMessage.class, RequestMessage::new))
                        .addLast(new ProtostuffEncoder<>(ResponseMessage.class))
                        .addLast(new RequestMessageHandler(serviceProviders));
                }
            });
            ChannelFuture channelFuture = bootstrap.bind().sync();
            this.channel = channelFuture.channel();
            this.localAddress = (InetSocketAddress)this.channel.localAddress();
            logger.info("exporter working at {}, {}", InetAddressUtils.getLocalAddress(), this.localAddress);
            this.working = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    @Override
    public <T> boolean support(ProviderDescriptor<T> desc) {
        return true;
    }

    @Override
    public <T> ServiceProvider<T> export(ProviderDescriptor<T> desc) {
        ServiceKey serviceKey = ServiceKey.of(desc);
        ServiceProvider<T> serviceProvider = DefaultServiceProvider.<T>builder()
            .protocol(supportedProtocol)
            .serviceHost(InetAddressUtils.getLocalAddress().getHostAddress())
            .servicePort(getServicePort())
            .serviceInterface(desc.getServiceInterface())
            .serviceVersion(desc.getServiceVersion())
            .serviceObject(desc.getServiceObject())
            .build();

        this.serviceProviders.put(serviceKey, serviceProvider);
        return serviceProvider;
    }

    @Override
    public <T> void remove(ProviderDescriptor<T> desc) {
        this.serviceProviders.remove(ServiceKey.of(desc));
    }

    public int getServicePort() {
        return this.localAddress.getPort();
    }

}