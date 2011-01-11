package org.detailsmatter.util.spring;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A set of {@link BeanOverrideDefinition} for a given bean
 * @author c_bbieth
 */
class BeanOverrideDefinitions {
	private final Map<Class<?>, BeanOverrideDefinition> overridingContextMap = Maps.newHashMap();

	public void registerOverride(BeanOverrideDefinition overrideDefinition) {
		BeanOverrideDefinition existingDefinition = overridingContextMap.get(overrideDefinition.overriddenContext);
		if (existingDefinition != null) {
			if (existingDefinition.hasPriorityOver(overrideDefinition)) {
				return;
			}
		}
		overridingContextMap.put(overrideDefinition.overridingContext, overrideDefinition);
	}

	public BeanOverrideDefinition findOverrideByOverridingContext(Class<?> context) {
		return overridingContextMap.get(context);
	}

	public void remove(BeanOverrideDefinition beanOverride) {
		overridingContextMap.remove(beanOverride.overridingContext);
	}

	public Collection<BeanOverrideDefinition> getAll() {
		return overridingContextMap.values();
	}
}