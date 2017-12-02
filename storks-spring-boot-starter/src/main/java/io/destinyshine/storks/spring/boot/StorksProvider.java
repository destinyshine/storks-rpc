package io.destinyshine.storks.spring.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StorksProvider {

    interface AutoFindInterface {}

    String USE_DEFAULT_VERSION = "";

    Class<?> serviceInterface() default AutoFindInterface.class;

    String serviceVersion() default USE_DEFAULT_VERSION;

}
