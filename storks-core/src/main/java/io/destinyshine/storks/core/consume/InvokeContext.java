package io.destinyshine.storks.core.consume;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InvokeContext {

    private static final ThreadLocal<InvokeContext> invokeContextLocal = ThreadLocal.withInitial(InvokeContext::new);

    private ConcurrentLinkedQueue<CompletableFuture<?>> promises = new ConcurrentLinkedQueue<>();

    private boolean asyncMode = false;

    public static <T> CompletableFuture<T> doInAsync(Runnable invocation) {
        InvokeContext context = invokeContextLocal.get();
        context.switchAsyncMode(true);
        invocation.run();
        context.switchAsyncMode(false);
        return context.popPromise();
    }

    public static InvokeContext getInvokeContext() {
        return invokeContextLocal.get();
    }

    private void switchAsyncMode(boolean asyncMode) {
        this.asyncMode = asyncMode;
    }

     void pushPromise(CompletableFuture promise) {
        promises.add(promise);
    }

    <T> CompletableFuture<T> popPromise() {
        return (CompletableFuture<T>)promises.poll();
    }

    public boolean isAsyncMode() {
        return asyncMode;
    }

}
