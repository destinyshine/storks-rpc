#自己动手实现RPC框架(4)－异步调用

## 索引
__replacing__

>代码库<br/>
>[https://github.com/destinyshine/storks-rpc.git](https://github.com/destinyshine/storks-rpc.git)

## 目的
在这一步，我们要客户端的异步调用。主要是客户端api上要增加一个功能，底层本身就是异步的。

## 设计思路

异步调用完全是客户端API的设计，不涉及底层通信机制和服务端的改造。

我们底层是基于netty nio 的reactor线程模式，本身就是异步的，所以我们只是在上层的使用方式增加一个功能，底层其实不用改造。
而且我们底层大量使用了CompletableFuture，在这个基础上实现这个功能非常简单。

### CompletableFuture简介
CompletableFuture是JDK 1.8新增的一个类，同时兼具了promise、future两种模式的功能，promise模式可以满足callback的要求，而且更为优雅。
CompletableFuture体现两java8 function的思想，api上大量使用两function。CompletableFuture和java 8 的Stream api设计上都很有rxJava的味道。

```java
//promise模式，感知到事件完成后，把promise map到另一个promise，类似Stream.map和Optional.map
//可以继续链式调用thenXxx的方法
completableFuture.thenApply(Function function)

//promise模式，感知到事件完成后，消费promise的数据，类似Optional.ifPresent
completableFuture.thenAccept(Consumer consumer)

//future模式，此类本身继承了Future类，直接使用即可
//CompletableFuture所实现的接口CompletionStage也提供了toCompletableFuture方法
completableFuture.get()
CompletionStage().toCompletableFuture().get()
```
所以，我们选择返回CompletableFuture来实现异步调用的返回，而不单独设计Future和Callback两种API。

### 目标效果

先看一下想要的效果，我希望不用为异步调用单独配置消费者，直接使用普通的消费者，doAsyncInvoke内部会做好临时切换。

```java
CompletionStage promise = InvocationContext.doAsyncInvoke(new Runnable() {
    @Override
    public void run() {
        computeService.add(a, b);
    }
})
```

用java 8的lambda表达式的话，就可以写成如下这样：

```java
InvocationContext.doAsyncInvoke(() -> computeService.add(a, b))
```

这种方式有两个优点

1. CompletableFuture兼具promise和future两种功能
2. 不用为异步调用单独配置消费者，直接使用普通的消费者，doAsyncInvoke内部会做好切换。

## 实现

### InvocationContext
这个是上下文工具类，通过它发起异步调用。

```java
package io.destinyshine.storks.core.consume.invoke;

import ...

public class InvocationContext {

    private static final ThreadLocal<InvocationContext> invokeContextLocal = ThreadLocal.withInitial(InvocationContext::new);

    private ConcurrentLinkedQueue<CompletionStage<?>> promises = new ConcurrentLinkedQueue<>();

    private boolean asyncResultMode = false;

    public static <T> CompletionStage<T> doAsyncInvoke(Runnable invocation) {
        InvocationContext context = invokeContextLocal.get();
        context.switchAsyncMode(true);
        invocation.run();
        context.switchAsyncMode(false);
        return context.popPromise();
    }

    public static InvocationContext getContext() {
        return invokeContextLocal.get();
    }

    private void switchAsyncMode(boolean asyncResultMode) {
        this.asyncResultMode = asyncResultMode;
    }

    void pushPromise(CompletionStage promise) {
        promises.add(promise);
    }

    <T> CompletionStage<T> popPromise() {
        return (CompletionStage<T>)promises.poll();
    }

    public boolean isAsyncResultMode() {
        return asyncResultMode;
    }

}

```

### RemoteProcedureInvoker

这个类增加异步模式下放置promise到context的逻辑，方便后续从context中取出。

```java
package io.destinyshine.storks.core.consume.invoke;

import ...

@Slf4j
public class DefaultRemoteProcedureInvoker extends RemoteServiceAccessor
    implements RemoteProcedureInvoker, AutoCloseable {

    @Override
    public Object invoke(RequestMessage requestMessage, ConsumerDescriptor<?> desc) throws Exception {
        ServiceReference refer = getServiceRefer(desc);
        CompletionStage<ResponseMessage> responsePromise = refer.invoke(requestMessage);

        //--add async promise handling. >>start
        if (InvocationContext.getContext().isAsyncResultMode()) {
            CompletionStage<Object> promise = responsePromise.thenApply(resp -> resp.getReturnValue());
            InvocationContext.getContext().pushPromise(promise);
            Class<?> returnType = requestMessage.getReturnType();
            if (returnType.isPrimitive()) {
                //返回默认值，防止空指针。
                return PrimitiveTypes.getPrimitiveDefaultValue(returnType);
            }
            return null;
        }
        //--add async promise handling. <<end

        return responsePromise.toCompletableFuture().get().getReturnValue();
    }

}
```

主要是增加了中间处理异步返回的那段代码那，其实这个时候没有真正得到响应，只是返回一个promise，并在未来会完成响应。

代码中还增加了一些针对java基本类型的处理，如果方法返回值为基本类型，那么返回null会报NullPointerException，所以需要返回对应基本类型的默认值--尽管这个值用不上。
PrimitiveTypes是自己写的一个工具类。

## 测试
基本上这个功能还是很简单的，以上一点简单的改造就实现了异步调用功能。只是一些修饰性的代码，底层网络层和方法调用执行等都不用变更。

现在可以测试一下了，改造一下客户端代码：

```java
package io.destinyshine.storks.sample.service;

import ...

public class AsyncClientMain {

    private static final Logger logger = LoggerFactory.getLogger(AsyncClientMain.class);

    private static final Random random = new Random();

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

        ConsumerDescriptor<ComputeService> desc = ConsumerBuilder
            .ofServiceInterface(ComputeService.class)
            .serviceVersion("1.0.0")
            .build();

        ConsumerProxyFactory consumerProxyFactory = new DefaultConsumerProxyFactory(invoker);

        ComputeService computeServiceConsumer = consumerProxyFactory.getConsumerProxy(desc);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    int a = random.nextInt(Integer.MAX_VALUE / 2);
                    int b = random.nextInt(Integer.MAX_VALUE / 2);
                    InvocationContext.doAsyncInvoke(() -> computeServiceConsumer.add(a, b))
                        .thenApply(result -> (int)result)
                        .thenAccept(sum -> logger.info("remote compute: {} + {} = {}, right:{}", a, b, sum, a + b == sum));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                invoker.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }));

        executorService.shutdown();
    }
}

```

执行之后，所有的结果都正确。测试完毕。。。。

