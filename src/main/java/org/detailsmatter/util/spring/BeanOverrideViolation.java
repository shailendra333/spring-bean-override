package org.detailsmatter.util.spring;

import java.lang.reflect.Method;

public class BeanOverrideViolation {
	public static class UnfulfilledOverride extends BeanOverrideViolation {
		public UnfulfilledOverride(BeanOverrideDefinition override) {
			super(override.overriddenBean.toString()
					+ " was expected to be overridden by " + override.overridingBean);
		}
	}

	public static class OverrideWrongContext extends BeanOverrideViolation {
		public OverrideWrongContext(BeanDefinition newBean, BeanDefinition existingBean,
				BeanOverrideDefinition newBeanOverride) {
			super( newBean.toString() + " is overriding " + existingBean
					+ " but is expected to override bean in context "
					+ newBeanOverride.overriddenContext.getSimpleName() );
		}
	}

	public static class NotOverriding extends BeanOverrideViolation {
		public NotOverriding(BeanDefinition newBean) {
			this(newBean, BeanOverrideDefinition.forMethod( newBean ));
		}
		
		public NotOverriding(BeanDefinition newBean, BeanOverrideDefinition overrideDefinition) {
			super( newBean.toString() + " is expected to override "
					+ overrideDefinition.overriddenBean + " but do not override anything" );
		}
	}

	public static class ShouldNotOverride extends BeanOverrideViolation {
		public ShouldNotOverride(BeanDefinition newBean, Class<?> existingContext) {
			this(newBean, new BeanDefinition(newBean.beanName, existingContext));
		}
		
		public ShouldNotOverride(BeanDefinition newBean, BeanDefinition existingBean) {
			super( newBean.toString() + " is incorrectly overriding " + existingBean.toString() );
		}
	}

	public static class MissingBeanAnnotation extends BeanOverrideViolation {
		public MissingBeanAnnotation(Method method) {
			super( "method " + method
					+ " has the @BeanOverride annotation but is missing the @Bean annotation." );
		}
	}

	private final String description;

	private BeanOverrideViolation(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		return (description == null) ? 0 : description.hashCode();
	}

	@Override
	public boolean equals( Object obj ) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeanOverrideViolation other = (BeanOverrideViolation) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals( other.description ))
			return false;
		return true;
	}
}
