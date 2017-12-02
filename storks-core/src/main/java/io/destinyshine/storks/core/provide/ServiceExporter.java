package io.destinyshine.storks.core.provide;

/**
 * Created by liujianyu.ljy on 17/8/20.
 *
 * @author liujianyu
 * @date 2017/08/20
 */
public interface ServiceExporter {

    <T> boolean support(ProviderDescriptor<T> desc);

    /**
     * export service
     *
     * @throws InterruptedException
     * @param desc
     */
    <T> ServiceProvider<T> export(ProviderDescriptor<T> desc);


    /**
     * export service
     *
     * @throws InterruptedException
     */
    <T> void remove(ProviderDescriptor<T> desc);

}
