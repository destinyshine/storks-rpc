package io.destinyshine.storks.core.consume.refer;

import io.destinyshine.storks.core.ServiceInstance;

/**
 * Created by liujianyu.ljy on 17/9/2.
 *
 * @author liujianyu.ljy
 * @date 2017/09/02
 */
public interface ServiceReferenceSupplier {

    ServiceReference getServiceReference(ServiceInstance instance) throws Exception;
}
