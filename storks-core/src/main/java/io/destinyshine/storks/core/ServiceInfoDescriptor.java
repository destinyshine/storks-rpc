package io.destinyshine.storks.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author destinyliu
 */
@Getter
@AllArgsConstructor
public abstract class ServiceInfoDescriptor<T> {

    protected final Class<T> serviceInterface;
    protected final String serviceVersion;

}
