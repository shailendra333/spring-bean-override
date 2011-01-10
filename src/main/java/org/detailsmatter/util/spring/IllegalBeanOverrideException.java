package org.detailsmatter.util.spring;

import java.util.Collections;
import java.util.Set;

public class IllegalBeanOverrideException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private Set<BeanOverrideViolation> violations;

	public IllegalBeanOverrideException() {
	}

	public IllegalBeanOverrideException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalBeanOverrideException(Set<BeanOverrideViolation> violations) {
		super(violationsToString(violations));
		this.violations = Collections.unmodifiableSet(violations);
	}

	private static String violationsToString(Set<BeanOverrideViolation> violations) {
		StringBuilder message = new StringBuilder();
		for (BeanOverrideViolation violation : violations) {
			if (message.length() > 0) {
				message.append(", ");
			}
			message.append(violation.getDescription());
		}
		return message.toString();
	}

	public IllegalBeanOverrideException(Throwable cause) {
		super(cause);
	}

	public Set<BeanOverrideViolation> getViolations() {
		return violations;
	}
}
