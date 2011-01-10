package org.detailsmatter.util.spring;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class BeanOverrideCheckerValidOverrideTest {
	@Configuration
	public static class App {
		@Bean
		public String bean1() {
			return "bean1";
		}
	}

	@Configuration
	@Import(App.class)
	public static class OverrideApp {
		@Bean
		@BeanOverride
		public String bean1() {
			return "override bean1";
		}
	}

	@Test
	public void beanOverrideShouldWork() {
		new BeanOverrideChecker().checkBeanOverride(OverrideApp.class);
	}

	@Configuration
	@Import(App.class)
	public static class OverrideAppWithTarget {
		@Bean
		@BeanOverride(context = App.class)
		public String bean1() {
			return "override bean1";
		}
	}

	@Test
	public void beanOverrideShouldWorkWithSpecificTarget() {
		new BeanOverrideChecker().checkBeanOverride(OverrideApp.class);
	}

	@Configuration
	@Import(OverrideAppWithTarget.class)
	public static class OverrideAppWithMultipleTarget {
		@Bean
		@BeanOverride(context = OverrideAppWithTarget.class)
		public String bean1() {
			return "override overriden bean1";
		}
	}

	@Test
	public void beanOverrideShouldWorkWithMultipleTarget() {
		new BeanOverrideChecker().checkBeanOverride(OverrideAppWithMultipleTarget.class);
	}
}
