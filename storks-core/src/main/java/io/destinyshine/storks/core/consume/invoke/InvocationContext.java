package io.destinyshine.storks.core.consume.invoke;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author liujianyu
 */
public class InvocationContext {

    private static final ThreadLocal<InvocationContext> invokeContextLocal = ThreadLocal.withInitial(InvocationContext::new);

    private ConcurrentLinkedQueue<CompletableFuture<?>> promises = new ConcurrentLinkedQueue<>();

    private boolean asyncResultMode = false;

    public static <T> CompletableFuture<T> forAsync(Runnable invocation) {
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

     void pushPromise(CompletableFuture promise) {
        promises.add(promise);
    }

    <T> CompletableFuture<T> popPromise() {
        return (CompletableFuture<T>)promises.poll();
    }

    public boolean isAsyncResultMode() {
        return asyncResultMode;
    }

}
