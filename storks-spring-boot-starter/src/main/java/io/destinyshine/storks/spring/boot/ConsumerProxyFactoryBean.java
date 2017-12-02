package io.destinyshine.storks.spring.boot;

import io.destinyshine.storks.core.consume.ConsumerDescriptor;
import io.destinyshine.storks.core.consume.ConsumerProxyFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * @author liujianyu
 */
public class ConsumerProxyFactoryBean<T> extends AbstractFactoryBean<T> {

    private ConsumerProxyFactory consumerProxyFactory;
    private ConsumerDescriptor<T> consumerDescriptor;

    public ConsumerProxyFactoryBean(ConsumerProxyFactory consumerProxyFactory,
                                    ConsumerDescriptor<T> consumerDescriptor) {
        this.consumerProxyFactory = consumerProxyFactory;
        this.consumerDescriptor = consumerDescriptor;
    }

    @Override
    public Class<?> getObjectType() {
        return consumerDescriptor.getServiceInterface();
    }

    @Override
    protected T createInstance() throws Exception {
        return consumerProxyFactory.getConsumerProxy(consumerDescriptor);
    }

}
