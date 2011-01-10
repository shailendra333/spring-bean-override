package org.detailsmatter.util.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.detailsmatter.util.assertion.Assert;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author <a href="mailto:biethb@gmail.com">Bruno Bieth</a>
 */
public class BeanOverrideChecker {
	static class BeanDefinition {
		private final String beanName;

		private final Class<?> context;

		/**
		 * might be null
		 */
		private final BeanOverride beanOverride;

		private Class<?> beanOverrideContext;

		public BeanDefinition(String beanName, Class<?> context, BeanOverride beanOverride) {
			Assert.notNull(beanName);
			Assert.notNull(context);

			this.beanName = beanName;
			this.context = context;
			this.beanOverride = beanOverride;
		}

		public boolean isOverride() {
			return beanOverride != null;
		}

		private Class<?> overridingContext() {
			if (beanOverrideContext == null) {
				if (beanOverride != null) {
					return beanOverrideContext = beanOverride.context();
				} else {
					return beanOverrideContext = Void.class;
				}
			}
			return beanOverrideContext;
		}

		@Override
		public String toString() {
			return context.getSimpleName() + "." + beanName;
		}

		public boolean isOverridingContext(Class<?> context) {
			return overridingContext() == Void.class || overridingContext() == context;
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

	/**
	 * @throws IllegalBeanOverrideException
	 */
	public void checkBeanOverride(Class<?> context) {
		Set<BeanOverrideViolation> violations = new HashSet<BeanOverrideViolation>();
		checkBeanOverride(context, new BeanDefinitions(), violations);
		if (!violations.isEmpty()) {
			throw new IllegalBeanOverrideException(violations);
		}
	}

	private void checkBeanOverride(Class<?> context, BeanDefinitions beanDefinitions, Set<BeanOverrideViolation> violations) {
		Assert.hasAnnotation(context, Configuration.class);

		// Important : depth first traversal
		checkBeanOverrideFromImport(context.getAnnotation(Import.class), beanDefinitions, violations);

		for (Method method : context.getDeclaredMethods()) {
			Bean beanAnnotation = method.getAnnotation(Bean.class);
			BeanOverride beanOverride = method.getAnnotation(BeanOverride.class);

			if (beanAnnotation != null) {
				String[] names = beanAnnotation.name();
				if (names.length > 0) {
					for (String name : names) {
						checkBeanOverride(new BeanDefinition(name, context, beanOverride), beanDefinitions, violations);
					}
				} else {
					checkBeanOverride(new BeanDefinition(method.getName(), context, beanOverride), beanDefinitions, violations);
				}
			} else if (beanOverride != null) {
				violations.add(new BeanOverrideViolation("method " + method
						+ " has the @BeanOverride annotation but is missing the @Bean annotation."));
			}
		}
	}

	private void checkBeanOverride(BeanDefinition newBean, BeanDefinitions beanDefinitions, Set<BeanOverrideViolation> violations) {
		BeanDefinition existingBean = beanDefinitions.findByName(newBean.beanName);
		if (existingBean == null) {
			if (newBean.isOverride()) {
				violations.add(new BeanOverrideViolation(newBean.toString() + " is expected to override a bean but do not override anything"));
			}
		} else {
			if (!newBean.isOverride()) {
				violations.add(new BeanOverrideViolation(newBean.toString() + " is overriding " + existingBean
						+ " but has not been annotated with @BeanOverride"));
			} else {
				if (!newBean.isOverridingContext(existingBean.context)) {
					violations.add(new BeanOverrideViolation(newBean.toString() + " is overriding " + existingBean
							+ " but is expected to override bean in context " + newBean.overridingContext()));
				}
			}
		}
		beanDefinitions.add(newBean);
	}

	private void checkBeanOverrideFromImport(Import importAnnotation, BeanDefinitions beanDefinitions, Set<BeanOverrideViolation> violations) {
		if (importAnnotation != null) {
			for (Class<?> context : importAnnotation.value()) {
				checkBeanOverride(context, beanDefinitions, violations);
			}
		}
	}
}
