package io.destinyshine.storks.core.consume.invoke;

import java.util.Optional;

import io.destinyshine.storks.core.ServiceInstance;
import io.destinyshine.storks.core.ServiceKey;
import io.destinyshine.storks.core.ServiceNotFoundException;
import io.destinyshine.storks.core.consume.ConsumerDescriptor;
import io.destinyshine.storks.core.consume.ServiceInstanceSelector;
import io.destinyshine.storks.core.consume.refer.ServiceReference;
import io.destinyshine.storks.core.consume.refer.ServiceReferenceSupplier;
import lombok.extern.slf4j.Slf4j;

/**
 * a base class for RemoteProcedureInvoker
 *
 * @author liujianyu
 * @date 2017/09/07
 */
@Slf4j
public abstract class RemoteServiceAccessor {

    protected ServiceInstanceSelector serviceInstanceSelector;
    protected ServiceReferenceSupplier serviceReferenceSupplier;

    protected ServiceReference getServiceRefer(ConsumerDescriptor<?> desc) throws Exception {
        ServiceKey serviceKey = ServiceKey.of(desc);

        while (true) {

            Optional<ServiceInstance> instOpt = serviceInstanceSelector.select(desc);

            if (instOpt.isPresent()) {
                ServiceInstance inst = instOpt.get();

                try {
                    return (serviceReferenceSupplier.getServiceReference(inst));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    serviceInstanceSelector.reportUnavailable(inst);
                }
            } else {
                throw new ServiceNotFoundException("cannot found service of " + serviceKey + " in registry.");
            }

        }
    }

    public void setServiceInstanceSelector(ServiceInstanceSelector serviceInstanceSelector) {
        this.serviceInstanceSelector = serviceInstanceSelector;
    }

    public void setServiceReferenceSupplier(ServiceReferenceSupplier serviceReferenceSupplier) {
        this.serviceReferenceSupplier = serviceReferenceSupplier;
    }
}
