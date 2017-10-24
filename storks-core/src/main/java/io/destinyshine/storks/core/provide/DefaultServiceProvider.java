package io.destinyshine.storks.core.provide;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liujianyu
 * @date 2017/09/16
 */
@Slf4j
@Data
@Builder
public class DefaultServiceProvider<T> implements ServiceProvider<T> {

    private final String protocol;

    private final String serviceHost;

    private final int servicePort;

    protected final Class<T> serviceInterface;

    protected final String serviceVersion;

    private final T serviceObject;

}
