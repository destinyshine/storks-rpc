package io.destinyshine.storks.discove;

import java.util.List;

import io.destinyshine.storks.core.ServiceInstance;
import io.destinyshine.storks.core.ServiceKey;

/**
 * @author liujianyu
 * @date 2017/09/10
 */
public interface ServiceInstanceList {

    public List<ServiceInstance> getServiceList(ServiceKey serviceKey);

    /**
     * 是否已经完成初始化
     *
     * @return true if initialized
     */
    public boolean isInitialized();
}
