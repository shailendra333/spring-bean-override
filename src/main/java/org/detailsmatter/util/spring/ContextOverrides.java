package org.detailsmatter.util.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContextOverrides {
	/**
	 * The overridden beans
	 */
	String[] beans();

	/**
	 * @Configuration class which gets overridden by {@link #with()}
	 */
	Class<?> of();

	/**
	 * @Configuration class which overrides {@link #of()}
	 */
	Class<?> with();
}
