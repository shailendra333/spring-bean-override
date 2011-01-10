package org.detailsmatter.util.spring;

public class BeanOverrideViolation {
	private final String description;

	public BeanOverrideViolation(String description) {
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
	public boolean equals(Object obj) {
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
		} else if (!description.equals(other.description))
			return false;
		return true;
	}
}
