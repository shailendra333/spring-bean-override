package org.detailsmatter.util.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Bean;

/**
 * Override a spring java config bean.
 * @see Bean
 * @author Bruno Bieth
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeanOverride {
	/**
	 * The context from which we override the bean. Void.class means any context.
	 */
	Class<?> context() default Void.class;
}
