package io.destinyshine.storks.core.consume;

import java.util.function.Consumer;

import io.destinyshine.storks.core.ServiceKey;

/**
 * Created by liujianyu.ljy on 17/9/10.
 *
 * @author liujianyu.ljy
 * @date 2017/09/10
 */
public class Subscription<T> {

    private final int index;

    private final ServiceKey serviceKey;

    private final Consumer<T> callback;

    public Subscription(int index, ServiceKey serviceKey, Consumer<T> callback) {
        this.index = index;
        this.serviceKey = serviceKey;
        this.callback = callback;
    }

    public ServiceKey getServiceKey() {
        return serviceKey;
    }

    public Consumer<T> getCallback() {
        return callback;
    }

    public long getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Subscription && this.index == ((Subscription)obj).index;
    }

}
