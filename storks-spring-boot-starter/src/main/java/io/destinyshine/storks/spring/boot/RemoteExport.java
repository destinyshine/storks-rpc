/*
 * Copyright (C) 2015-2016 Philipp Nanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package io.destinyshine.storks.spring.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable automatic remote export and proxy creation of Spring beans.
 * 
 * @author Philipp Nanz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RemoteExport {

	/**
	 * The path mapping URI to use for this service (e.g. "/AccountService").
	 * Defaults to the simple name of the interface on which this annotation is declared.
	 */
	String mappingPath() default "";

	/**
	 * Set whether to register a RemoteInvocationTraceInterceptor for exported services.
	 * 
	 * @see org.springframework.remoting.support.RemoteExporter#setRegisterTraceInterceptor(boolean)
	 */
	boolean registerTraceInterceptor() default true;
}