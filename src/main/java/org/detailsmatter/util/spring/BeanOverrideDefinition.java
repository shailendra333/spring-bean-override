/**
 * 
 */
package org.detailsmatter.util.spring;

import org.detailsmatter.util.assertion.Assert;

class BeanOverrideDefinition {

	public final String beanId;
	public final Class<?> overriddenContext;
	public final Class<?> overridingContext;
	public final OverridePriority priority;
	public final boolean overridesSpecificContext;
	public final BeanDefinition overriddenBean;
	public final BeanDefinition overridingBean;

	private BeanOverrideDefinition(String beanId, Class<?> overriddenContext, Class<?> overridingContext, OverridePriority priority) {
		Assert.notNull(beanId);
		Assert.notNull(overriddenContext);
		Assert.notNull(overridingContext);
		Assert.notNull(priority);
		Assert.that(overridingContext != Void.class, "Overriding context should not be Void.class");

		this.beanId = beanId;
		this.overriddenContext = overriddenContext;
		this.overridingContext = overridingContext;
		this.priority = priority;
		this.overridesSpecificContext = overriddenContext != Void.class;
		this.overriddenBean = new BeanDefinition(beanId, overriddenContext);
		this.overridingBean = new BeanDefinition(beanId, overridingContext);
	}

	public boolean hasPriorityOver(BeanOverrideDefinition other) {
		return this.priority.isHigherThan(other.priority);
	}

	public static BeanOverrideDefinition forMethod(String beanId, Class<?> overriddenContext, Class<?> overridingContext) {
		return new BeanOverrideDefinition(beanId, overriddenContext, overridingContext, OverridePriority.METHOD);
	}

	public static BeanOverrideDefinition forMethod(BeanDefinition overridingBeanDefinition) {
		return new BeanOverrideDefinition(overridingBeanDefinition.beanName, Void.class, overridingBeanDefinition.context,
				OverridePriority.METHOD);
	}

	public static BeanOverrideDefinition forMethod(BeanDefinition overriddenBeanDefinition, Class<?> overridingContext) {
		return new BeanOverrideDefinition(overriddenBeanDefinition.beanName, overriddenBeanDefinition.context, overridingContext,
				OverridePriority.METHOD);
	}

	public static BeanOverrideDefinition forClass(String beanId, Class<?> overriddenContext, Class<?> overridingContext) {
		return new BeanOverrideDefinition(beanId, overriddenContext, overridingContext, OverridePriority.CLASS);
	}

	@Override
	public String toString() {
		return "BeanOverride[" + overriddenBean + " <-- " + overridingBean + "]";
	}
}