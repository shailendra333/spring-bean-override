package org.detailsmatter.util.spring;

public enum OverridePriority {
	/**
	 * @see BeanOverride
	 */
	METHOD(0), 
	
	/**
	 * @see ContextOverride
	 * @see ContextOverrides
	 */
	CLASS(1);

	private int priority;

	private OverridePriority(int priority) {
		this.priority = priority;
	}

	public boolean isHigherThan(OverridePriority priority) {
		return this.priority > priority.priority;
	}
}
