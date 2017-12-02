package io.destinyshine.storks.core.provide;

/**
 * Created by liujianyu.ljy on 17/8/20.
 *
 * @author liujianyu
 * @date 2017/08/20
 */
public interface ServiceProviderListener {

    /**
     * "add" event fired by ProviderManager
     * @param desc
     */
    void onServiceProviderAdded(ServiceProvider<?> desc);

    /**
     * "remove" event fired by ProviderManager
     * @param desc
     */
    void onServiceProviderRemoved(ServiceProvider<?> desc);

}
