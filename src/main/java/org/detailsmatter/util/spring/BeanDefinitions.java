/**
 * 
 */
package org.detailsmatter.util.spring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class BeanDefinitions {
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