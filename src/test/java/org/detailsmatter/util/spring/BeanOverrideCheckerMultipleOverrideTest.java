package org.detailsmatter.util.spring;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class BeanOverrideCheckerMultipleOverrideTest {

	@Configuration
	public static class MultipleBeansApp {
		@Bean
		public String bean1() {
			return "bean1";
		}

		@Bean
		public String bean2() {
			return "bean1";
		}

		@Bean
		public String bean3() {
			return "bean1";
		}
	}

	@Configuration
	@Import(MultipleBeansApp.class)
	public static class MultipleBeansOverrideApp {
		@Bean
		@BeanOverride
		public String bean1() {
			return "override bean1";
		}

		@Bean
		@BeanOverride
		public String bean2() {
			return "override bean2";
		}

		@Bean
		public String bean3() {
			return "override bean3";
		}
	}

	@Test(expected = IllegalBeanOverrideException.class)
	public void missingBeanOverrideOnLastBeanTest() {
		new BeanOverrideChecker().checkBeanOverride(MultipleBeansOverrideApp.class);
	}

	@Configuration
	@Import(MultipleBeansApp.class)
	public static class WorkingMultipleBeansOverrideApp {
		@Bean
		@BeanOverride
		public String bean1() {
			return "override bean1";
		}

		@Bean
		@BeanOverride
		public String bean2() {
			return "override bean2";
		}

		@Bean
		@BeanOverride
		public String bean3() {
			return "override bean3";
		}
	}

	@Test(expected = IllegalBeanOverrideException.class)
	public void multipleBeanOverrideTest() {
		new BeanOverrideChecker().checkBeanOverride(MultipleBeansOverrideApp.class);
	}
}
