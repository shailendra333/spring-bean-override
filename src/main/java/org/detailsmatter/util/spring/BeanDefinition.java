/**
 * 
 */
package org.detailsmatter.util.spring;

import org.detailsmatter.util.assertion.Assert;

class BeanDefinition {
	public final String beanName;
	public final Class<?> context;
	
	private final String toString;

	public BeanDefinition(String beanName, Class<?> context) {
		Assert.notNull(beanName);
		Assert.notNull(context);

		this.beanName = beanName;
		this.context = context;

		if (context != Void.class) {
			toString = context.getSimpleName() + "." + beanName;
		} else {
			toString = beanName;
		}
	}

	@Override
	public String toString() {
		return toString;
	}
}