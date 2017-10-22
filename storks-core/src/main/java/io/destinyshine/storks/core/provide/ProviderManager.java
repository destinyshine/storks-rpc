package io.destinyshine.storks.core.provide;

import java.util.List;
import java.util.Map;

import io.destinyshine.storks.core.ServiceKey;
import io.destinyshine.storks.core.StorksApplication;

/**
 * Created by liujianyu.ljy on 17/8/20.
 *
 * @author liujianyu.ljy
 * @date 2017/08/20
 */
public interface ProviderManager {

    <T> void addProvider(ProviderDescriptor<T> desc);

    <T> void removeProvider(ProviderDescriptor<T> desc);

    void addServiceProviderListener(ServiceProviderListener listener);

    void removeServiceProviderListener(ServiceProviderListener listener);

    StorksApplication getApplication();

    Map<ServiceKey, List<ServiceProvider<?>>> getProviders();

}
