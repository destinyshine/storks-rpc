package io.destinyshine.storks.core.consume;

/**
 * Created by liujianyu.ljy on 17/9/2.
 *
 * @author liujianyu
 * @date 2017/09/02
 */
public interface ConsumerProxyFactory {

    /**
     * get a consumer by desc.
     *
     * @return consumer proxy object.
     */
    public <T> T getConsumerProxy(ConsumerDescriptor<T> desc);

}
