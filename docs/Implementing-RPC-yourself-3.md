#自己动手实现RPC框架(3)－服务注册&服务发现

## 索引
__replacing__

>代码库<br/>
>[https://github.com/destinyshine/storks-rpc.git](https://github.com/destinyshine/storks-rpc.git)

## 目的
在这一步，我们要实现服务注册与发现。

## 注册中心
为了实现服务注册与发现，我们需要一个支持服务注册的组件。

### 注册中心选型
通常来讲，如果我们要构建一个服务注册中心，很多人的第一反应是可以用zookeeper来实现，然而实际上zookeeper并不是一个合适的服务注册中心。

* zookeeper 较为底层，只是一个node列表维护的程序，不具备服务注册&服务发现的常用语意，需要自己去实现。
* 在CAP原理的抉择中，zookeeper牺牲了可用性，用以保证一致性和分区容错性。zookeeper本身设计用来做分布式应用的协调者，这么设计是可以理解的。但对于服务注册，可用性是优先级最高的。

*注：其实zookeeper本身的可用性也是非常高的。只是在zookeeper的设计思想中可用性的优先级较一致性和分区容错性低，主要是在极端情况下的抉择。*

除了zookeeper，还有很多专门的服务注册管理软件，专门针对服务注册和发现而设计，比较符合我们的场景，较为常见、使用广泛的有eureka何consul：

* netflix eureka，微服务设计的引领者netfix推出的服务注册&发现组件，知名度较高。spring cloud也对eureka做了封装和支持，使用非常方便，与同样来自netflix公司的客户端负载均衡组件ribbon可以完美配合。
* consul，同样是一个专门的服务注册&发现组件，consul的api更为友好，原生的API是restful api，还有各种编程语言封装的客户端。


<table>
    <thead>
        <tr>
            <th width="26%">Feature</th>
            <th width="21%">Consul</th>
            <th width="17%">zookeeper</th>
            <th width="18%">etcd</th>
            <th width="18%">euerka</th></tr>
    </thead>
    <tbody>
        <tr>
            <td>服务健康检查</td>
            <td>服务状态，内存，硬盘等</td>
            <td>(弱)长连接，keepalive</td>
            <td>连接心跳</td>
            <td>可配支持</td></tr>
        <tr>
            <td>多数据中心</td>
            <td>支持</td>
            <td>—</td>
            <td>—</td>
            <td>—</td></tr>
        <tr>
            <td>kv存储服务</td>
            <td>支持</td>
            <td>支持</td>
            <td>支持</td>
            <td>—</td></tr>
        <tr>
            <td>一致性</td>
            <td>raft</td>
            <td>paxos</td>
            <td>raft</td>
            <td>—</td></tr>
        <tr>
            <td>cap</td>
            <td>ca</td>
            <td>cp</td>
            <td>cp</td>
            <td>ap</td></tr>
        <tr>
            <td>使用接口(多语言能力)</td>
            <td>支持http和dns</td>
            <td>客户端</td>
            <td>http/grpc</td>
            <td>http（sidecar）</td></tr>
        <tr>
            <td>watch支持</td>
            <td>全量/支持long polling</td>
            <td>支持</td>
            <td>支持 long polling</td>
            <td>支持 long polling/大部分增量</td></tr>
        <tr>
            <td>自身监控</td>
            <td>metrics</td>
            <td>—</td>
            <td>metrics</td>
            <td>metrics</td></tr>
        <tr>
            <td>安全</td>
            <td>acl /https</td>
            <td>acl</td>
            <td>https支持（弱）</td>
            <td>—</td></tr>
        <tr>
            <td>spring cloud集成</td>
            <td>已支持</td>
            <td>已支持</td>
            <td>已支持</td>
            <td>已支持</td></tr>
    </tbody>
</table>

还有一个细节，eureka只支持server级别的注册，不支持service级别的注册。而且consul支持resetful客户端，也就是只要支持http协议即可，我们直接使用restful接口，不使用其他第三方客户端，可以少依赖很多jar。

因此，最终我们选择consul作为服务注册表，但这个不是强耦合的，我们定义好核心接口后，可以自由实现其他的服务注册表。

## 设计

回顾前两个章节，其实核心的接口已经定义好了。我们只需要替换那两个接口的实现方式即可。

### consul客户端
consul的java客户端有consul-api和consul-client两种，本质上都是对restful http api的封装。

在这里我们会再抽象抽象出一层本系统ConsulClient，底层再调用具体的Consul客户端。可以自由切换各种Consul客户端。

由于Consul原生的API就是restful http client，所以我们没必要依赖第三方的客户端，自己实现一个restful客户端即可。客户端我们基于netty 自己封装一个，由于程序已经依赖netty作为基础网络开发包，因此不需要更多依赖。

consul的restful接口提供的数据格式为json，json处理我们自己实现了一个简单的JsonParser，参见另一篇文章。

### 客户端实现
回顾上一章客户端的时序图：<br/>
![客户端时序图](https://raw.githubusercontent.com/destinyshine/storks-rpc/master/docs/images/simple-client-seq.png)

注意`ServiceInstanceSelector`接口，这个接口的作用是查找一个服务实例（ServiceInstance），在上一章我们实现的是一个简单的直连查找（Direct ServiceInstanceSelector）。要实现客户端的服务发现，我们只需要替换这个接口的实现类即可。改成从服务注册中心查找服务实例。

### 服务端实现
服务端的`ServiceExporter`需要增加一个`RegistryBasedExporter`的实现类，`RegistryBasedExporter`的export逻辑转发到内部的`delegate ServiceExporter`，并额外增加register和deregister的逻辑，此处是`装饰者模式`。

### 异步IO和CompletableFuture
程序中使用到IO的时候都是使用NIO，NIO是非阻塞的，因此等待一个读或者写完成需要使用一些异步交互的工具，在此我们使用CompletableFuture，在程序中，凡是涉及异步响应的，都使用CompletableFuture来实现。

CompletableFuture是java 8新增的类，在java中实现了类似Promise的模式，同时兼具callable和Future的特质。

### 组件和领域层次的对应关系
同一个概念在不同的层次深度，有不同的表述形式，对于一个服务，在应用程序的不同领域层次里有不同的类描述，其关系如下：
![服务表述类和层次对应关系](https://raw.githubusercontent.com/destinyshine/storks-rpc/master/docs/images/compoent-cs-overview-layer.png)


### 代码实现

#### 客户端: RegistryBasedServiceList
```java
package io.destinyshine.storks.discove;

import ...

@Slf4j
public class RegistryBasedServiceList implements ServiceInstanceList {

    private final Set<ServiceKey> watchedServiceKeys = new HashSet<>();

    private final Map<ServiceKey, List<ServiceInstance>> serviceListCache = new ConcurrentHashMap<>();

    private final ServiceRegistry serviceRegistry;

    public RegistryBasedServiceList(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public List<ServiceInstance> getServiceList(ServiceKey serviceKey) {
        if (watchedServiceKeys.contains(serviceKey)) {
            return serviceListCache.get(serviceKey);
        } else {
            try {
                CompletionStage<List<ServiceInstance>> serviceListFuture = serviceRegistry.discover(serviceKey);
                List<ServiceInstance> serviceList = serviceListFuture.toCompletableFuture().get();

                serviceListCache.put(serviceKey, serviceList);

                //watch
                serviceRegistry.subscribe(serviceKey, (serviceListNew) -> {
                    serviceListCache.put(serviceKey, serviceListNew);
                });
                watchedServiceKeys.add(serviceKey);
                return serviceList;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public boolean isInitialized() {
        return true;
    }
}

```
#### 服务端: RegistryBasedExporter
```java
package io.destinyshine.storks.core;

import ...

public class RegistryBasedExporter implements ServiceExporter {

    private final ServiceRegistry registry;

    private final ServiceExporter delegate;

    private final StorksApplication application;

    public RegistryBasedExporter(ServiceRegistry registry,
                                 ServiceExporter delegate,
                                 StorksApplication application) {
        Objects.requireNonNull(registry, "registry cannot be null.");
        Objects.requireNonNull(delegate, "delegate cannot be null.");
        Objects.requireNonNull(application, "application cannot be null.");
        this.registry = registry;
        this.delegate = delegate;
        this.application = application;
    }

    @Override
    public <T> boolean support(final ProviderDescriptor<T> desc) {
        return delegate.support(desc);
    }

    @Override
    public <T> ServiceProvider<T> export(ProviderDescriptor<T> desc) {
        ServiceProvider<T> provider = delegate.export(desc);
        registry.register(providerToInstance(provider));
        return provider;
    }

    @Override
    public <T> void remove(ProviderDescriptor<T> desc) {
        delegate.remove(desc);
    }

    private <T> ServiceInstance providerToInstance(ServiceProvider<T> provider) {
        ServiceInstance serviceInstance = ServiceInstance.builder()
            .appName(application.getAppName())
            .protocol("storks")
            .host(InetAddressUtils.getLocalAddress().getHostAddress())
            .port(provider.getServicePort())
            .serviceInterface(provider.getServiceInterface().getName())
            .serviceVersion(provider.getServiceVersion())
            .build();
        return serviceInstance;
    }
}

```

#### ConsulRegistry
```java
package io.destinyshine.storks.registry.consul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.destinyshine.storks.core.ServiceInstance;
import io.destinyshine.storks.core.ServiceKey;
import io.destinyshine.storks.core.ServiceNotFoundException;
import io.destinyshine.storks.core.ServiceRegistry;
import io.destinyshine.storks.core.consume.Subscription;
import io.destinyshine.storks.registry.consul.client.ConsulClient;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toList;

/**
 * @author destinyliu
 */
@Slf4j
public class ConsulRegistry implements ServiceRegistry {

    /**
     * default healthCheckInterval in seconds
     */
    public static final int DEFAULT_HEALTH_CHECK_INTERVAL = 5 * 60;

    /**
     * healthCheckInterval in seconds
     */
    private final int healthCheckInterval;

    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);

    private final Map<String, ConsulService> registeredServices = new HashMap<>();

    private final ConsulClient client;

    private final Map<ServiceKey, Long> lastConsulIndexMap = new ConcurrentHashMap<>();

    private final Map<ServiceKey, List<Subscription<List<ServiceInstance>>>> allWatchers = new HashMap<>();

    private final int fetchIntervalSeconds;
    private final int reportIntervalSeconds;

    private final AtomicInteger lastSubscriptionIndex = new AtomicInteger();

    public ConsulRegistry(ConsulClient client) {
        this(client, DEFAULT_HEALTH_CHECK_INTERVAL);
    }

    public ConsulRegistry(ConsulClient client, int healthCheckInterval) {
        this.client = client;
        this.healthCheckInterval = healthCheckInterval;
        this.fetchIntervalSeconds = (int)(healthCheckInterval* 0.6);
        this.reportIntervalSeconds = (int)(healthCheckInterval* 0.6);
        this.startAutoFetch();
        this.startAutoPush();
    }

    private void startAutoPush() {
        executorService.scheduleWithFixedDelay(
            this::reportToRegistry,
            reportIntervalSeconds, reportIntervalSeconds, TimeUnit.SECONDS
        );
    }

    private void startAutoFetch() {
        executorService.scheduleWithFixedDelay(
            this::notifyWatchersIfUpdated,
            reportIntervalSeconds, fetchIntervalSeconds, TimeUnit.SECONDS
        );
    }

    private void reportToRegistry() {
        registeredServices.values()
            .stream()
            .map(ConsulService::getId)
            .forEach(client::checkPassService);
    }

    private void notifyWatchersIfUpdated() {
        allWatchers.forEach((key, watchers) ->
            discoverInternal(key, lastConsulIndexMap.get(key))
                .thenAccept(servicesOpt ->
                    servicesOpt.ifPresent(serviceList ->
                        watchers.forEach(subscription ->
                            subscription.getCallback().accept(serviceList)
                        )
                    )
                )
        );

    }

    @Override
    public void register(ServiceInstance instance) {
        ConsulService service = ConsulServiceHelper.toConsulService(instance, healthCheckInterval);
        logger.info("will register, service={}", instance);
        //register async
        client.registerService(service)
            .handle((e, result) -> {
                if (Objects.nonNull(e)) {
                    logger.error("error on register, service={}", service, e);
                } else {
                    logger.info("register success, service={}, will check pass", service);
                    client.checkPassService(service.getId())
                        .exceptionally(checkError -> {
                            logger.error("error on checkPass, service={}", service, checkError);
                            return null;
                        });
                    //add to local list.
                    registeredServices.put(service.getId(), service);
                }
                return null;
            });
    }

    @Override
    public void unregister(ServiceInstance instance) {
        String serviceId = ConsulServiceHelper.buildConsulServiceId(instance);
        //remove from local list.
        registeredServices.remove(serviceId);
        //checkFail async
        client.checkFailService(serviceId)
            .handle((e, result) -> {
                if (Objects.nonNull(e)) {
                    logger.error("error on checkFail, serviceId={}", serviceId, e);
                } else {
                    logger.info("checkFail success, serviceId={}, then do unregister", serviceId);
                    client.unregisterService(serviceId)
                        .exceptionally(checkError -> {
                            logger.error("error on unregister, serviceId={}", serviceId, checkError);
                            return null;
                        });
                }
                return null;
            });
    }

    @Override
    public CompletionStage<List<ServiceInstance>> discover(ServiceKey serviceKey) {
        CompletionStage<Optional<List<ServiceInstance>>> serviceListNew = discoverInternal(serviceKey, -1);
        return serviceListNew.thenApply(servicesOpt -> servicesOpt.get());
    }

    @Override
    public Subscription<List<ServiceInstance>> subscribe(ServiceKey serviceKey,
                                                         Consumer<List<ServiceInstance>> callback) {
        Subscription<List<ServiceInstance>> subscription = new Subscription<>(
            lastSubscriptionIndex.incrementAndGet(),
            serviceKey,
            callback
        );

        allWatchers.compute(serviceKey, (key, watchers) -> {
            if (watchers != null) {
                watchers.add(subscription);
                return watchers;
            }

            List<Subscription<List<ServiceInstance>>> newSubscriptions = new ArrayList<>();
            newSubscriptions.add(subscription);
            return newSubscriptions;
        });
        return subscription;
    }

    @Override
    public void unsubscribe(Subscription<?> subscription) {
        this.allWatchers.computeIfPresent(
            subscription.getServiceKey(),
            (key, subs) -> subs.stream()
                .filter(Predicate.isEqual(subscription).negate())
                .collect(toList())
        );
    }

    private CompletionStage<Optional<List<ServiceInstance>>> discoverInternal(ServiceKey serviceKey,
                                                                                long lastConsulIndexId) {
        CompletionStage<ConsulResponse<List<ConsulService>>> responseFuture;
        responseFuture = lookupConsulService(serviceKey, lastConsulIndexId);
        return responseFuture.thenApply(response -> {
            if (response == null) {
                throw new ServiceNotFoundException("response=null, can not found any service in registry.");
            }
            List<ConsulService> services = response.getValue();
            if (services != null && !services.isEmpty()) {

                if (response.getConsulIndex() == lastConsulIndexId) {
                    return Optional.empty();
                }

                lastConsulIndexMap.put(serviceKey, response.getConsulIndex());

                List<ServiceInstance> serviceInstances = response.getValue().stream()
                    .map(service -> ConsulServiceHelper.toServiceInstance(service))
                    .collect(toList());

                return Optional.of(serviceInstances);

            } else {
                throw new ServiceNotFoundException("can not found any service in registry.");
            }
        });

    }

    /**
     * directly fetch consul service data.
     *
     * @param serviceKey
     * @return ConsulResponse or null
     */
    private CompletionStage<ConsulResponse<List<ConsulService>>> lookupConsulService(ServiceKey serviceKey,
                                                                                       long lastConsulIndexId) {
        CompletionStage<ConsulResponse<List<ConsulService>>> response = client.lookupHealthService(
            serviceKey.toString(),
            lastConsulIndexId
        );
        return response;
    }

    @Override
    public void close() {

    }

}

```

#### NettyHttpClient
```java
package io.destinyshine.storks.http;

import ...

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

```

### 测试类
客户端和服务端大体与前几章相同，只是替换一下某些接口的实现类。这一步我们还没有集成Spring，没有DI和IoC，只能手工编码了。
#### 客户端
```java
package io.destinyshine.storks.sample.service;

import ...

public class ClientMain {

    private static final Logger logger = LoggerFactory.getLogger(ClientMain.class);

    public static void main(String[] args) throws Exception {
        ConsulRegistry registry = new ConsulRegistry(new RestConsulClient("127.0.0.1", 8500));

        DefaultRemoteProcedureInvoker invoker = new DefaultRemoteProcedureInvoker();
        invoker.setServiceReferenceSupplier(new NettyNioServiceReferenceSupplier());
        invoker.setServiceInstanceSelector(
            new DynamicListServiceInstanceSelector(
                new RegistryBasedServiceList(registry),
                new RandomLoadBalanceStrategy()
            )
        );

        ConsumerDescriptor<HelloService> desc = ConsumerBuilder
            .ofServiceInterface(HelloService.class)
            .serviceVersion("1.0.0")
            .build();

        ConsumerProxyFactory consumerProxyFactory = new DefaultConsumerProxyFactory(invoker);

        HelloService helloServiceConsumer = consumerProxyFactory.getConsumerProxy(desc);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executorService.submit(() -> {
                String input = null;
                String result = null;
                try {
                    input = "tom-" + Thread.currentThread().getName() + "," + finalI;
                    result = helloServiceConsumer.hello(input);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                logger.info("input={}, get result: {}", input, result);
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                invoker.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }));

        //System.exit(0);
    }
}

```

### 服务端
```java
package io.destinyshine.storks.sample.service;

import io.destinyshine.storks.core.RegistryBasedExporter;
import io.destinyshine.storks.core.StorksApplication;
import io.destinyshine.storks.core.provide.DefaultProviderManager;
import io.destinyshine.storks.core.provide.ProviderDescriptor;
import io.destinyshine.storks.registry.consul.ConsulRegistry;
import io.destinyshine.storks.registry.consul.client.rest.RestConsulClient;
import io.destinyshine.storks.sample.service.api.HelloService;
import io.destinyshine.storks.sample.service.impl.HelloServiceImpl;
import io.destinyshine.storks.support.provide.NettyNioServiceExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liujianyu
 */
public class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) throws Exception {

        logger.info("--server main--");

        StorksApplication app = new StorksApplication("testProvider");

        ProviderDescriptor desc = new ProviderDescriptor(HelloService.class, "1.0.0", new HelloServiceImpl());

        NettyNioServiceExporter internalExporter = new NettyNioServiceExporter(app,0);
        ConsulRegistry registry = new ConsulRegistry(new RestConsulClient("127.0.0.1", 8500));
        RegistryBasedExporter exporter = new RegistryBasedExporter(registry, internalExporter, app);

        DefaultProviderManager providerManager = new DefaultProviderManager(exporter);
        providerManager.setApplication(app);

        //add provider
        providerManager.addProvider(desc);

        logger.info("exporter started.");

    }
}

```

## 后续
后续我们要实现的还有以下特性：
* 异步调用，future or callback
* spring boot集成
* 调用连跟踪
 