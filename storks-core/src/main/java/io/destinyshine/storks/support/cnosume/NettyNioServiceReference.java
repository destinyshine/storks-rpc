package io.destinyshine.storks.support.cnosume;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;
import io.destinyshine.storks.core.consume.refer.ServiceReference;
import io.destinyshine.storks.support.ProtostuffDecoder;
import io.destinyshine.storks.support.ProtostuffEncoder;
import io.destinyshine.storks.support.StorksProtocolHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liujianyu
 */
public class NettyNioServiceReference implements AutoCloseable, ServiceReference {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Object channelWriteLock = new Object();

    private ConcurrentLinkedQueue<CompletableFuture<ResponseMessage>>
        responsePromises = new ConcurrentLinkedQueue<CompletableFuture<ResponseMessage>>();

    private final String remoteHost;
    private final int remotePort;

    private Channel channel;

    private long connectedTime;


    public NettyNioServiceReference(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void connect() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.remoteAddress(new InetSocketAddress(remoteHost, remotePort));
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                    super.channelActive(ctx);
                }

                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new ProtostuffEncoder<>(RequestMessage.class))
                        .addLast(StorksProtocolHelper.newFrameDecoder())
                        .addLast(new ProtostuffDecoder<>(ResponseMessage.class, ResponseMessage::new))
                        .addLast(new SimpleChannelInboundHandler<ResponseMessage>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ResponseMessage msg)
                                throws Exception {
                                CompletableFuture<ResponseMessage> promise;
                                if ((promise = responsePromises.poll()) != null) {
                                    promise.complete(msg);
                                } else {
                                    promise.completeExceptionally(new IllegalStateException("remote server closed!"));
                                    logger.error("remote server closed!");
                                }
                            }
                        });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            this.channel = channelFuture.channel();
            this.connectedTime = System.currentTimeMillis();
            channelFuture.addListener(future -> {
                if (future.isSuccess()) {
                    logger.debug(future.toString() + "client connected");
                } else {
                    logger.debug(future.toString() + "server attemp failed", future.cause());
                }

            });
        } finally {

        }
    }

    @Override
    public CompletionStage<ResponseMessage>  invoke(RequestMessage requestMessage) {
        CompletableFuture<ResponseMessage> promise = new CompletableFuture<>();
        synchronized (channelWriteLock) {
            this.responsePromises.add(promise);
            this.channel.writeAndFlush(requestMessage);
        }
        return promise;
    }

    public long getConnectedTime() {
        return connectedTime;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public String toString() {
        return "NettyNioServiceRefer{" +
            "remoteHost='" + remoteHost + '\'' +
            ", remotePort=" + remotePort +
            '}';
    }

    @Override
    public void close() {
        channel.close().awaitUninterruptibly();
    }
}