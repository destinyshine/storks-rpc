///*
// * Copyright (C) 2015-2016 Philipp Nanz
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package io.destinyshine.storks.spring.boot;
//
//import java.util.Collections;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.config.BeanDefinition;
//import org.springframework.beans.factory.support.AbstractBeanDefinition;
//import org.springframework.beans.factory.support.BeanDefinitionBuilder;
//import org.springframework.beans.factory.support.BeanDefinitionRegistry;
//import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
//import org.springframework.core.annotation.AnnotationUtils;
//import org.springframework.core.type.AnnotationMetadata;
//import org.springframework.core.type.MethodMetadata;
//import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
//import org.springframework.util.Assert;
//import org.springframework.util.ClassUtils;
//
//import com.github.philippn.springremotingautoconfigure.annotation.RemoteExport;
//import com.github.philippn.springremotingautoconfigure.util.RemotingUtils;
//
///**
// * @author Philipp Nanz
// */
//public class HttpInvokerServiceExporterRegistrar implements ImportBeanDefinitionRegistrar {
//
//	final static Logger logger = LoggerFactory.getLogger(HttpInvokerServiceExporterRegistrar.class);
//
//	private final Set<String> alreadyExportedSet =
//			Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
//
//	/* (non-Javadoc)
//	 * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
//	 */
//	@Override
//	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
//		for (String beanName : registry.getBeanDefinitionNames()) {
//			BeanDefinition definition = registry.getBeanDefinition(beanName);
//			String className = definition.getBeanClassName();
//			if (className == null && definition.getSource() instanceof MethodMetadata) {
//				className = ((MethodMetadata) definition.getSource()).getReturnTypeName();
//			}
//			if (className != null) {
//				try {
//					Class<?> resolvedClass = ClassUtils.forName(className, null);
//					if (resolvedClass.isInterface()) {
//						if (AnnotationUtils.isAnnotationDeclaredLocally(RemoteExport.class, resolvedClass)) {
//							setupExport(resolvedClass, beanName, registry);
//						}
//					} else {
//						Class<?>[] beanInterfaces = resolvedClass.getInterfaces();
//						for (Class<?> clazz : beanInterfaces) {
//							if (AnnotationUtils.isAnnotationDeclaredLocally(RemoteExport.class, clazz)) {
//								setupExport(clazz, beanName, registry);
//							}
//						}
//					}
//				} catch (ClassNotFoundException e) {
//					throw new IllegalStateException("Unable to inspect class " +
//							definition.getBeanClassName() + " for @RemoteExport annotations");
//				}
//			}
//		}
//	}
//
//	protected void setupExport(Class<?> clazz, String beanName, BeanDefinitionRegistry registry) {
//		Assert.isTrue(clazz.isInterface(),
//				"Annotation @RemoteExport may only be used on interfaces");
//
//		if (alreadyExportedSet.contains(clazz.getName())) {
//			return;
//		}
//		alreadyExportedSet.add(clazz.getName());
//
//		BeanDefinitionBuilder builder = BeanDefinitionBuilder
//				.genericBeanDefinition(HttpInvokerServiceExporter.class)
//				.setLazyInit(true)
//				.addPropertyReference("service", beanName)
//				.addPropertyValue("serviceInterface", clazz)
//				.addPropertyValue("registerTraceInterceptor",
//						getRegisterTraceInterceptor(clazz));
//		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
//		beanDefinition.setSynthetic(true);
//
//		String mappingPath = RemotingUtils.buildMappingPath(clazz);
//		registry.registerBeanDefinition(mappingPath, beanDefinition);
//
//		logger.info("Mapping HttpInvokerServiceExporter for "
//				+ clazz.getSimpleName() + " to [" + mappingPath + "]");
//	}
//
//	protected Boolean getRegisterTraceInterceptor(Class<?> clazz) {
//		RemoteExport definition = AnnotationUtils.findAnnotation(clazz, RemoteExport.class);
//		return Boolean.valueOf(definition.registerTraceInterceptor());
//	}
//}