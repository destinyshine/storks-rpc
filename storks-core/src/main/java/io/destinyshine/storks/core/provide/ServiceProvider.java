package io.destinyshine.storks.core.provide;

import io.destinyshine.storks.core.RequestMessage;
import io.destinyshine.storks.core.ResponseMessage;

/**
 * Created by liujianyu.ljy on 17/9/18.
 *
 * @author liujianyu.ljy
 * @date 2017/09/18
 */
public interface ServiceProvider<T> {

    ResponseMessage execute(RequestMessage request);

    String getProtocol();

    String getServiceHost();

    int getServicePort();

    Class<T> getServiceInterface();

    String getServiceVersion();

    T getServiceObject();

}
