/**
 * 
 */
package org.detailsmatter.util.spring;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

class BeanOverridesContext {
	private final BeanDefinitions beanDefinitions;

	private final Map<String, BeanOverrideDefinitions> overrides = Maps.newHashMap();

	public BeanOverridesContext() {
		beanDefinitions = new BeanDefinitions();
	}

	private BeanOverrideDefinitions findOverrideDefinitions( String beanId ) {
		BeanOverrideDefinitions overridingDefinitions = overrides.get( beanId );
		if (overridingDefinitions == null) {
			overridingDefinitions = new BeanOverrideDefinitions();
			overrides.put( beanId, overridingDefinitions );
		}
		return overridingDefinitions;
	}

	public void registerOverride( BeanOverrideDefinition beanOverrideDefinition ) {
		findOverrideDefinitions( beanOverrideDefinition.beanId ).registerOverride( beanOverrideDefinition );
	}

	public Set<BeanOverrideViolation> getUnfulfilledOverrides() {
		Set<BeanOverrideViolation> violations = Sets.newHashSet();
		for (BeanOverrideDefinition overrideDefinition : getAllOverrideDefinition()) {
			violations.add( new BeanOverrideViolation.UnfulfilledOverride( overrideDefinition ) );
		}
		return violations;
	}

	private Set<BeanOverrideDefinition> getAllOverrideDefinition() {
		Set<BeanOverrideDefinition> flatDefinitions = Sets.newHashSet();
		for (BeanOverrideDefinitions beanDefinitions : overrides.values()) {
			flatDefinitions.addAll( beanDefinitions.getAll() );
		}
		return flatDefinitions;
	}

	public BeanOverrideViolation addBean( String beanId, Class<?> context ) {
		BeanDefinition newBean = new BeanDefinition( beanId, context );
		BeanOverrideDefinition newBeanOverride = findOverrideDefinition( newBean );

		BeanDefinition existingBean = beanDefinitions.findByName( beanId );

		removeOverride( newBeanOverride );

		if (existingBean == null) {
			if (newBeanOverride != null) {
				return new BeanOverrideViolation.NotOverriding( newBean, newBeanOverride );
			}
		} else {
			if (newBeanOverride == null) {
				return new BeanOverrideViolation.ShouldNotOverride( newBean, existingBean );
			} else {
				if ((newBeanOverride.overridesSpecificContext)
						&& (newBeanOverride.overriddenContext != existingBean.context)) {
					return new BeanOverrideViolation.OverrideWrongContext( newBean, existingBean,
							newBeanOverride );
				}
			}
		}

		// we could add more context here, like a link to the overriden bean if
		// there's one
		beanDefinitions.add( newBean );
		return null;
	}

	private void removeOverride( BeanOverrideDefinition beanOverride ) {
		if (beanOverride != null) {
			findOverrideDefinitions( beanOverride.beanId ).remove( beanOverride );
		}
	}

	private BeanOverrideDefinition findOverrideDefinition( BeanDefinition newBean ) {
		return findOverrideDefinitions( newBean.beanName ).findOverrideByOverridingContext(
				newBean.context );
	}
}