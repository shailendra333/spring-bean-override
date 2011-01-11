package org.detailsmatter.util.spring;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Post processor that checks at runtime bean overrides.
 * @author <a href="mailto:biethb@gmail.com">Bruno Bieth</a>
 */
public class BeanOverrideCheckerPostProcessor implements BeanPostProcessor {

	private final BeanOverrideChecker checker;

	@Configuration
	public static class Context {
		@Bean
		public BeanOverrideCheckerPostProcessor beanOverrideCheckerPostProcessor() {
			return new BeanOverrideCheckerPostProcessor();
		}
	}

	public BeanOverrideCheckerPostProcessor() {
		checker = new BeanOverrideChecker();
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (AopUtils.isCglibProxyClass(bean.getClass())) {
			checkConfigurationClass(bean.getClass().getSuperclass());
		} else {
			checkConfigurationClass(bean.getClass());
		}
		return bean;
	}

	private void checkConfigurationClass(Class<?> configClass) {
		if (configClass.getAnnotation(Configuration.class) != null) {
			checker.checkBeanOverride(configClass);
		}
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
