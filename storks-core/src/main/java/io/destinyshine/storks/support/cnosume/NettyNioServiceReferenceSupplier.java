package io.destinyshine.storks.support.cnosume;

import io.destinyshine.storks.core.ServiceInstance;
import io.destinyshine.storks.core.consume.refer.AbstractServiceReferenceSupplier;
import io.destinyshine.storks.core.consume.refer.ServiceReference;
import lombok.extern.slf4j.Slf4j;

/**
 * NIO consuming connection supplier.
 *
 * @author liujianyu
 * @date 2017/09/03
 */
@Slf4j
public class NettyNioServiceReferenceSupplier extends AbstractServiceReferenceSupplier {

    @Override
    public ServiceReference createServiceReferInternal(ServiceInstance instance) throws Exception {
        logger.info("will create refer of instance {}", instance);
        String remoteHost = instance.getHost();
        int remotePort = instance.getPort();
        NettyNioServiceReference con = new NettyNioServiceReference(remoteHost, remotePort);
        con.connect();
        return con;
    }
}
