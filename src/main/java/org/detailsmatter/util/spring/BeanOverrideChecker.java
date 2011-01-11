package org.detailsmatter.util.spring;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.detailsmatter.util.assertion.Assert;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * Performs bean override checks
 * @author <a href="mailto:biethb@gmail.com">Bruno Bieth</a>
 */
public class BeanOverrideChecker {
	/**
	 * @throws IllegalBeanOverrideException
	 */
	public void checkBeanOverride(Class<?> context) {
		Set<BeanOverrideViolation> violations = getBeanOverrideViolations(context);
		if (!violations.isEmpty()) {
			throw new IllegalBeanOverrideException(violations);
		}
	}
	
	public Set<BeanOverrideViolation> getBeanOverrideViolations(Class<?> context) {
		Set<BeanOverrideViolation> violations = new HashSet<BeanOverrideViolation>();
		BeanOverridesContext beanOverridesContext = new BeanOverridesContext();
		checkBeanOverride(context, beanOverridesContext, violations, new HashSet<Class<?>>());
		violations.addAll(beanOverridesContext.getUnfulfilledOverrides());
		return violations;
	}

	private void checkBeanOverride(Class<?> context, BeanOverridesContext beanOverridesContext, Set<BeanOverrideViolation> violations,
			Set<Class<?>> processedClasses) {
		if (processedClasses.contains(context)) {
			return;
		}

		Assert.hasAnnotation(context, Configuration.class);
		processedClasses.add(context);

		registerOverridesFromContextOverridesAnnotation(context, beanOverridesContext);

		// Important : depth first traversal
		checkBeanOverrideFromImport(context.getAnnotation(Import.class), beanOverridesContext, violations, processedClasses);

		for (Method method : context.getDeclaredMethods()) {
			Bean beanAnnotation = method.getAnnotation(Bean.class);
			BeanOverride beanOverride = method.getAnnotation(BeanOverride.class);

			if (beanAnnotation != null) {
				String[] beanIds = beanAnnotation.name();
				if (beanIds.length > 0) {
					for (String beanId : beanIds) {
						addBean(context, beanOverridesContext, beanId, beanOverride, violations);
					}
				} else {
					addBean(context, beanOverridesContext, method.getName(), beanOverride, violations);
				}
			} else if (beanOverride != null) {
				violations.add(new BeanOverrideViolation.MissingBeanAnnotation(method));
			}
		}
	}

	private void addBean(Class<?> context, BeanOverridesContext overridingContext, String beanId, BeanOverride beanOverride,
			Set<BeanOverrideViolation> violations) {
		registerOverrideFromBeanOverrideAnnotation(beanOverride, beanId, context, overridingContext);
		BeanOverrideViolation violation = overridingContext.addBean(beanId, context);
		if (violation != null) {
			violations.add(violation);
		}
	}

	private void registerOverrideFromBeanOverrideAnnotation(BeanOverride beanOverride, String name, Class<?> overridingContext,
			BeanOverridesContext beanOverridesContext) {
		if (beanOverride != null) {
			beanOverridesContext.registerOverride(BeanOverrideDefinition.forMethod( name, beanOverride.context(), overridingContext));
		}
	}

	private void registerOverridesFromContextOverridesAnnotation(Class<?> context, BeanOverridesContext beanOverridesContext) {
		ContextOverrides contextOverrides = context.getAnnotation(ContextOverrides.class);
		if( contextOverrides != null ) {
			for( ContextOverride contextOverride : contextOverrides.value() ) {
				registerOverridesFromContextOverrideAnnotation( beanOverridesContext, contextOverride );
			}
		}

		ContextOverride contextOverride = context.getAnnotation(ContextOverride.class);
		if (contextOverride != null) {
			registerOverridesFromContextOverrideAnnotation( beanOverridesContext, contextOverride );
		}
	}

	private void registerOverridesFromContextOverrideAnnotation( BeanOverridesContext beanOverridesContext,
			ContextOverride contextOverrides ) {
		for (String bean : contextOverrides.beans()) {
			beanOverridesContext.registerOverride(
					BeanOverrideDefinition.forClass( bean, contextOverrides.of(), contextOverrides.with()));
		}
	}

	private void checkBeanOverrideFromImport(Import importAnnotation, BeanOverridesContext beanOverridesContext,
			Set<BeanOverrideViolation> violations, Set<Class<?>> processedClasses) {
		if (importAnnotation != null) {
			for (Class<?> context : importAnnotation.value()) {
				checkBeanOverride(context, beanOverridesContext, violations, processedClasses);
			}
		}
	}
}
