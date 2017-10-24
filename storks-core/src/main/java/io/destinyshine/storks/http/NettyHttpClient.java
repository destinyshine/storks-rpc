package io.destinyshine.storks.http;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.destinyshine.storks.utils.MapUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyHttpClient {
    public static final int DEFAULT_MAX_RESPONSE_SIZE = 1024 * 1024 * 10;

    private Bootstrap bootstrap;

    {
        int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(ioWorkerCount);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new HttpClientCodec());
                    pipeline.addLast(new HttpObjectAggregator(DEFAULT_MAX_RESPONSE_SIZE));
                }
            });
        this.bootstrap = bootstrap;
    }

    public CompletionStage<FullHttpResponse> get(String url) {
        return get(url, Collections.EMPTY_MAP);
    }

    public CompletionStage<FullHttpResponse> get(String url, Map<String, List<String>> headers) {
        return doRequest(HttpMethod.GET, url, Unpooled.EMPTY_BUFFER, headers);
    }

    public CompletionStage<FullHttpResponse> put(String url, ByteBuf body) {
        return doRequest(HttpMethod.PUT, url, body, Collections.EMPTY_MAP);
    }

    protected CompletionStage<FullHttpResponse> doRequest(HttpMethod method, String url, ByteBuf body,
                                                            Map<String, List<String>> headers) {
        URI uri = URI.create(url);
        HttpRequest httpRequest = createNettyRequest(method, uri, body, headers);
        return execute(uri, httpRequest);
    }

    private FullHttpRequest createNettyRequest(HttpMethod method, URI uri, ByteBuf body,
                                               Map<String, List<String>> headers) {

        String authority = uri.getRawAuthority();
        String path = uri.toString().substring(uri.toString().indexOf(authority) + authority.length());

        FullHttpRequest nettyRequest = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, method, path, body);

        HttpHeaders nettyHeaders = nettyRequest.headers();

        nettyHeaders.set(HttpHeaderNames.HOST, uri.getHost());
        nettyHeaders.set(HttpHeaderNames.CONNECTION, "close");

        if (MapUtils.isNotEmpty(headers)) {
            headers.forEach((name, values) -> nettyHeaders.add(name, values));
        }

        if (!nettyHeaders.contains(HttpHeaderNames.CONTENT_LENGTH) && body.readableBytes() > 0) {
            nettyHeaders.set(HttpHeaderNames.CONTENT_LENGTH, body.readableBytes());
        }

        return nettyRequest;
    }

    private CompletionStage<FullHttpResponse> execute(URI uri, HttpRequest httpRequest) {
        CompletableFuture<FullHttpResponse> responseFuture = new CompletableFuture<>();
        bootstrap.connect(uri.getHost(), getPort(uri))
            .addListener((ChannelFutureListener)future -> {

                if (future.isSuccess()) {
                    Channel channel = future.channel();
                    channel.pipeline().addLast(new RequestExecuteHandler(responseFuture));
                    channel.writeAndFlush(httpRequest);
                } else {
                    responseFuture.completeExceptionally(future.cause());
                }
            });
        return responseFuture;
    }

    private static int getPort(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(uri.getScheme())) {
                port = 80;
            } else if ("https".equalsIgnoreCase(uri.getScheme())) {
                port = 443;
            }
        }
        return port;
    }

    /**
     * A SimpleChannelInboundHandler to update the given SettableListenableFuture.
     */
    private static class RequestExecuteHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

        private final CompletableFuture<FullHttpResponse> responseFuture;

        public RequestExecuteHandler(CompletableFuture<FullHttpResponse> responseFuture) {
            this.responseFuture = responseFuture;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext context, FullHttpResponse response) throws Exception {
            int statusCode = response.status().code();
            if (statusCode >= 200 && statusCode < 300) {

                this.responseFuture.complete(response);
            } else {
                String responseMessage = response.toString();
                this.responseFuture.completeExceptionally(new HttpStatusCodeException(
                    statusCode,
                    String.format("exception status %s %s, response:\n %s", statusCode, response.status().reasonPhrase(), responseMessage)
                ));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
            this.responseFuture.completeExceptionally(cause);
        }
    }

}
