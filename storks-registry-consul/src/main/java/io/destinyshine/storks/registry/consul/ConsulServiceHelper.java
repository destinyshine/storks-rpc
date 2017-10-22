package io.destinyshine.storks.registry.consul;

import java.util.ArrayList;
import java.util.List;

import io.destinyshine.storks.core.ServiceInstance;
import io.destinyshine.storks.core.ServiceKey;
import org.apache.commons.lang3.StringUtils;

/**
 * @author destinyliu
 */
public class ConsulServiceHelper {

    /**
     * 有motan的group生成consul的serivce name
     *
     * @param instance
     * @return
     */
    public static String obtainConsulServiceName(ServiceInstance instance) {
        return instance.getServiceInterface() + ':' + instance.getServiceVersion();
    }

    /**
     * 根据motan的url生成consul的serivce id。 serviceid 包括ip＋port＋rpc服务的接口类名
     *
     * @param instance
     * @return
     */
    public static String buildConsulServiceId(ServiceInstance instance) {
        if (instance == null) {
            return null;
        }

        return new StringBuilder().append(instance.getProtocol()).append(":").append(instance.getHost()).append(":")
            .append(instance.getPort()).append(":").append(instance
                .getServiceInterface()).append(":").append(instance.getServiceVersion()).toString();
    }

    /**
     * 根据服务的url生成consul对应的service
     *
     * @param instance
     * @return
     */
    public static ConsulService toConsulService(ServiceInstance instance, int healthCheckInterval) {

        ConsulService service = new ConsulService();
        service.setServiceKey(ServiceKey.of(instance.getServiceInterface(), instance.getServiceVersion()));
        service.setAddress(instance.getHost());
        service.setId(ConsulServiceHelper.buildConsulServiceId(instance));
        service.setName(ConsulServiceHelper.obtainConsulServiceName(instance));
        service.setPort(instance.getPort());
        service.setCheckTtl(healthCheckInterval);

        List<String> tags = new ArrayList<String>();
        tags.add(ConsulConstants.TAG_PREFIX_APP + instance.getAppName());
        service.setTags(tags);

        return service;
    }

    public static ServiceInstance toServiceInstance(ConsulService service) {
        String serviceKey = service.getName();
        String[] interfaceAndVersion = StringUtils.split(serviceKey, ':');

        ServiceInstance inst = ServiceInstance.builder()
            .host(service.getAddress())
            .protocol("storks")
            .port(service.getPort())
            .serviceInterface(interfaceAndVersion[0])
            .serviceVersion(interfaceAndVersion[1])
            .build();
        return inst;
    }
}
