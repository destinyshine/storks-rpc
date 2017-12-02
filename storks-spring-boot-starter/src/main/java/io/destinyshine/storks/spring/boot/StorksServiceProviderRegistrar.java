package io.destinyshine.storks.spring.boot;

import io.destinyshine.storks.core.provide.ProviderDescriptor;
import io.destinyshine.storks.spring.boot.StorksProvider.AutoFindInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;

/**
 * @author liujianyu
 */
@Slf4j
public class StorksServiceProviderRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private StorksProperties storksProperties;

    private ConfigurableEnvironment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        bindProperties();

        boolean providerEnabled = storksProperties.isProviderEnabled();
        boolean consumerEnabled = storksProperties.isConsumerEnabled();

        if (!providerEnabled && !consumerEnabled) {
            logger.error("both providerEnabled and consumerEnabled are false, please check, just warn.");
        }

        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition definition = registry.getBeanDefinition(beanName);
            String className = definition.getBeanClassName();
            //if bean from @Bean
            if (className == null && definition.getSource() instanceof MethodMetadata) {
                //MethodMetadata methodMetadata = (MethodMetadata)definition.getSource();
                //className = methodMetadata.getReturnTypeName();
                //StorksProvider s = methodMetadata.getAllAnnotationAttributes()
                //TODO:: method provider handle.
                continue;
            }
            if (className != null) {
                if (providerEnabled) {
                    try {
                        Class<?> resolvedClass = ClassUtils.forName(className, null);
                        if (AnnotationUtils.isAnnotationDeclaredLocally(StorksProvider.class, resolvedClass)) {
                            StorksProvider providerAn = resolvedClass.getAnnotation(StorksProvider.class);
                            setupServiceProvider(providerAn, resolvedClass, beanName, registry);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("Unable to inspect class " + definition.getBeanClassName()
                            + " for @RemoteExport annotations");
                    }
                }

                if (consumerEnabled) {
                    //ReflectionUtils.doWithLocalFields(targetClass, new ReflectionUtils.FieldCallback() {
                    //    @Override
                    //    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    //        AnnotationAttributes ann = findAutowiredAnnotation(field);
                    //        if (ann != null) {
                    //            if (Modifier.isStatic(field.getModifiers())) {
                    //                if (logger.isWarnEnabled()) {
                    //                    logger.warn("Autowired annotation is not supported on static fields: " + field);
                    //                }
                    //                return;
                    //            }
                    //            boolean required = determineRequiredStatus(ann);
                    //            currElements.add(new AutowiredFieldElement(field, required));
                    //        }
                    //    }
                    //});
                }
            }
        }
    }

    protected void setupServiceProvider(StorksProvider providerAn, Class<?> beanType, String beanName, BeanDefinitionRegistry registry) {
        String anServiceVersion = providerAn.serviceVersion();
        Class<?> anServiceInterface = providerAn.serviceInterface();
        Class<?>[] interfaces = beanType.getInterfaces();
        if (anServiceInterface == AutoFindInterface.class) {
            if (interfaces.length == 0) {
                String msg  = String.format("type %s not implemented any interface, which beanName is %s", beanType, beanName);
                throw new IllegalStateException(msg);
            } else if (interfaces.length != 1) {
                throw new IllegalStateException(String.format(
                    "unable to auto find serviceInterface of type %s, it has %s interfaces.",
                    beanType,
                    interfaces.length
                ));
            } else {
                anServiceInterface = interfaces[0];
            }
        } else if (!ArrayUtils.contains(interfaces, anServiceInterface)) {
            throw new IllegalStateException(String.format(
                "error serviceInterface in @StorksProvider, bean type %s not implemented interface %s",
                beanType,
                anServiceInterface
            ));
        }
        if (StorksProvider.USE_DEFAULT_VERSION.equals(anServiceVersion)) {
            anServiceVersion = storksProperties.getProvider().getDefaultVersion();
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition(ProviderDescriptor.class)
            .setLazyInit(true)
            .addConstructorArgValue(anServiceInterface)
            .addConstructorArgValue(anServiceVersion)
            .addConstructorArgReference(beanName);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinition.setSynthetic(true);

        registry.registerBeanDefinition(beanName + "Provider", beanDefinition);

        logger.info("add service provider:{}:{}:{} of serviceBean:{}", anServiceInterface.getName(), anServiceVersion, beanName);
    }

    private void bindProperties() {
        StorksProperties storksProperties = new StorksProperties();
        MutablePropertySources propertySources = environment.getPropertySources();

        PropertiesConfigurationFactory<Object> factory = new PropertiesConfigurationFactory(storksProperties);
        factory.setPropertySources(propertySources);
        factory.setTargetName(StorksProperties.PROP_PREFIX);

        try {
            factory.bindPropertiesToTarget();
        } catch (Exception e) {
            throw new BeanCreationException("Could not bind environment to " + StorksProperties.class, e);
        }

        this.storksProperties = storksProperties;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment)environment;
    }

}
