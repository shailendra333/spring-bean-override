package org.detailsmatter.util.spring;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.detailsmatter.util.assertion.Assert;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author <a href="mailto:biethb@gmail.com">Bruno Bieth</a>
 */
public class BeanOverrideChecker {
	static class BeanDefinition {
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

	static class BeanDefinitions {
		private final Set<BeanDefinition> beanDefinitions = new HashSet<BeanDefinition>();
		private final Map<String, BeanDefinition> beanDefinitionsNameMap = new HashMap<String, BeanDefinition>();

		public void add(BeanDefinition beanDefinition) {
			// replace the existing bean, in order to reproduce Spring behavior
			BeanDefinition existingBean = beanDefinitionsNameMap.get(beanDefinition.beanName);
			if (existingBean != null) {
				beanDefinitions.remove(existingBean);
			}

			beanDefinitions.add(beanDefinition);
			beanDefinitionsNameMap.put(beanDefinition.beanName, beanDefinition);
		}

		/**
		 * @return <code>null</code> if not found
		 */
		public BeanDefinition findByName(String beanName) {
			return beanDefinitionsNameMap.get(beanName);
		}
	}

	static class BeanOverrideDefinition {
		public final String beanId;
		public final Class<?> overriddenContext;
		public final Class<?> overridingContext;
		public final OverridePriority priority;
		public final boolean overridesSpecificContext;
		public final BeanDefinition overriddenBean;
		public final BeanDefinition overridingBean;

		public BeanOverrideDefinition(String beanId, Class<?> overriddenContext, Class<?> overridingContext, OverridePriority priority) {
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
	}

	/**
	 * A set of {@link BeanOverrideDefinition} for a given bean
	 * @author c_bbieth
	 */
	static class BeanOverrideDefinitions {
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

	static class BeanOverridesContext {
		private final BeanDefinitions beanDefinitions;

		private final Map<String, BeanOverrideDefinitions> overrides = Maps.newHashMap();

		public BeanOverridesContext() {
			beanDefinitions = new BeanDefinitions();
		}

		private BeanOverrideDefinitions findOverrideDefinitions(String beanId) {
			BeanOverrideDefinitions overridingDefinitions = overrides.get(beanId);
			if (overridingDefinitions == null) {
				overridingDefinitions = new BeanOverrideDefinitions();
				overrides.put(beanId, overridingDefinitions);
			}
			return overridingDefinitions;
		}

		public void registerOverride(String beanId, Class<?> overriddenContext, Class<?> overridingContext, OverridePriority priority) {
			findOverrideDefinitions(beanId).registerOverride(new BeanOverrideDefinition(beanId, overriddenContext, overridingContext, priority));
		}

		public Set<BeanOverrideViolation> getUnfulfilledOverrides() {
			Set<BeanOverrideViolation> violations = Sets.newHashSet();
			for (BeanOverrideDefinition overrideDefinition : getAllOverrideDefinition()) {
				violations.add(new BeanOverrideViolation(overrideDefinition.overriddenBean.toString() + " was expected to be overridden by "
						+ overrideDefinition.overridingBean));
			}
			return violations;
		}

		private Set<BeanOverrideDefinition> getAllOverrideDefinition() {
			Set<BeanOverrideDefinition> flatDefinitions = Sets.newHashSet();
			for (BeanOverrideDefinitions beanDefinitions : overrides.values()) {
				flatDefinitions.addAll(beanDefinitions.getAll());
			}
			return flatDefinitions;
		}

		public BeanOverrideViolation addBean(String beanId, Class<?> context) {
			BeanDefinition newBean = new BeanDefinition(beanId, context);
			BeanOverrideDefinition newBeanOverride = findOverrideDefinition(newBean);

			BeanDefinition existingBean = beanDefinitions.findByName(beanId);

			if (existingBean == null) {
				if (newBeanOverride != null) {
					return new BeanOverrideViolation(newBean.toString() + " is expected to override a bean but do not override anything");
				}
			} else {
				if (newBeanOverride == null) {
					return new BeanOverrideViolation(newBean.toString() + " is incorrectly overriding " + existingBean);
				} else {
					if ((newBeanOverride.overridesSpecificContext) && (newBeanOverride.overriddenContext != existingBean.context)) {
						return new BeanOverrideViolation(newBean.toString() + " is overriding " + existingBean
								+ " but is expected to override bean in context " + newBeanOverride.overriddenContext);
					}
				}
			}

			// we could add more context here, like a link to the overriden bean if there's one
			beanDefinitions.add(newBean);
			removeOverride(newBeanOverride);
			return null;
		}

		private void removeOverride(BeanOverrideDefinition beanOverride) {
			if (beanOverride != null) {
				findOverrideDefinitions(beanOverride.beanId).remove(beanOverride);
			}
		}

		private BeanOverrideDefinition findOverrideDefinition(BeanDefinition newBean) {
			return findOverrideDefinitions(newBean.beanName).findOverrideByOverridingContext(newBean.context);
		}
	}

	/**
	 * @throws IllegalBeanOverrideException
	 */
	public void checkBeanOverride(Class<?> context) {
		Set<BeanOverrideViolation> violations = new HashSet<BeanOverrideViolation>();
		BeanOverridesContext beanOverridesContext = new BeanOverridesContext();
		checkBeanOverride(context, beanOverridesContext, violations, new HashSet<Class<?>>());
		violations.addAll(beanOverridesContext.getUnfulfilledOverrides());
		if (!violations.isEmpty()) {
			throw new IllegalBeanOverrideException(violations);
		}
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
				violations.add(new BeanOverrideViolation("method " + method
						+ " has the @BeanOverride annotation but is missing the @Bean annotation."));
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
			beanOverridesContext.registerOverride(name, beanOverride.context(), overridingContext, OverridePriority.METHOD);
		}
	}

	private void registerOverridesFromContextOverridesAnnotation(Class<?> context, BeanOverridesContext beanOverridesContext) {
		ContextOverride contextOverrides = context.getAnnotation(ContextOverride.class);
		if (contextOverrides != null) {
			for (String bean : contextOverrides.beans()) {
				beanOverridesContext.registerOverride(bean, contextOverrides.of(), contextOverrides.with(), OverridePriority.CLASS);
			}
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
