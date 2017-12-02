package io.destinyshine.storks.spring.boot;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.destinyshine.storks.core.consume.ConsumerBuilder;
import io.destinyshine.storks.core.consume.ConsumerDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author liujianyu
 */
@Slf4j
public class ConsumerAnnotationPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
    implements InstantiationAwareBeanPostProcessor, BeanFactoryAware, Serializable,PriorityOrdered {

    private ConfigurableListableBeanFactory beanFactory;
    private Set<String> consumingMetadataCache = new HashSet<>();
    private Set<ConsumerDescriptor> consumerDescCache = new HashSet<>();
    private int order = Ordered.LOWEST_PRECEDENCE - 5;

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : beanClass.getName());
        // Quick check on the concurrent map first, with minimal locking.
        synchronized (consumingMetadataCache) {
            if (!this.consumingMetadataCache.contains(cacheKey)) {
                processConsumingMetadata(beanClass);
                this.consumingMetadataCache.add(cacheKey);
            }
        }

        return true;
    }

    private void processConsumingMetadata(Class<?> beanClass) {
        if (logger.isDebugEnabled()) {
            logger.debug("process class [{}]", beanClass);
        }
        List<ConsumerDescriptor<?>> consumerDescriptors = findConsumerMetadata(beanClass);
        for (ConsumerDescriptor<?> desc : consumerDescriptors) {
            synchronized (consumerDescCache) {
                if (!consumerDescCache.contains(desc)) {
                    AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
                        .genericBeanDefinition(ConsumerProxyFactoryBean.class)
                        .setLazyInit(false)
                        .addConstructorArgReference("consumerProxyFactory")
                        .addConstructorArgValue(desc)
                        .getBeanDefinition();

                    beanDefinition.setSynthetic(true);
                    String consumerBeanName = desc.getServiceInterface().getName() + ":" + desc
                        .getServiceVersion();
                    ((BeanDefinitionRegistry)beanFactory).registerBeanDefinition(consumerBeanName, beanDefinition);
                    if (logger.isInfoEnabled()) {
                        logger.info("register consumer beanDefinition {}", consumerBeanName);
                    }
                    consumerDescCache.add(desc);
                }

            }
        }
    }

    private List<ConsumerDescriptor<?>> findConsumerMetadata(final Class<?> clazz) {
        Class<?> targetClass = clazz;
        final LinkedList<ConsumerDescriptor<?>> consumerDescriptors = new LinkedList<>();

        do {
            ReflectionUtils.doWithLocalFields(targetClass, field -> {
                StorksConsumer ann = findConsumerAnnotation(field);
                if (ann != null) {
                    ConsumerDescriptor<?> consumerDescriptor = ConsumerBuilder
                        .ofServiceInterface(field.getType())
                        .serviceVersion(ann.serviceVersion())
                        .build();
                    consumerDescriptors.add(consumerDescriptor);
                }
            });

            ReflectionUtils.doWithLocalMethods(targetClass, method -> {
                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
                if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }
                StorksConsumer ann = findConsumerAnnotation(bridgedMethod);
                if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("StorksConsumer annotation is not supported on static methods: " + method);
                        }
                        return;
                    }
                    if (method.getParameterTypes().length == 0) {
                        if (logger.isWarnEnabled()) {
                            logger.warn(
                                "StorksConsumer annotation should be used on methods with parameters: " + method);
                        }
                    }
                    ConsumerDescriptor<?> consumerDescriptor = ConsumerBuilder
                        .ofServiceInterface(method.getParameterTypes()[0])
                        .serviceVersion(ann.serviceVersion())
                        .build();
                    consumerDescriptors.add(consumerDescriptor);
                }
            });
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        return consumerDescriptors;
    }

    private StorksConsumer findConsumerAnnotation(AccessibleObject ao) {
        StorksConsumer consumerAnn = AnnotationUtils.findAnnotation(ao, StorksConsumer.class);
        return consumerAnn;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory)beanFactory;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
