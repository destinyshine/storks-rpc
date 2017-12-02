package io.destinyshine.storks.core.provide;

/**
 * Created by liujianyu.ljy on 17/9/18.
 *
 * @author liujianyu
 * @date 2017/09/18
 */
public interface ServiceProvider<T> {

    String getProtocol();

    String getServiceHost();

    int getServicePort();

    Class<T> getServiceInterface();

    String getServiceVersion();

    T getServiceObject();

}
