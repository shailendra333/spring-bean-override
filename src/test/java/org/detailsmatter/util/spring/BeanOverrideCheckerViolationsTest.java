package org.detailsmatter.util.spring;

import static org.hamcrest.MatcherAssert.assertThat;

import org.detailsmatter.util.assertion.Assert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class BeanOverrideCheckerViolationsTest {
	@Configuration
	public static class BaseContext {
		@Bean
		public String beanA() {
			return "bean A";
		}

		@Bean
		public String beanC() {
			return "beanC";
		}
	}

	@Configuration
	@Import(BaseContext.class)
	public static class OneViolationContext {
		@Bean
		@BeanOverride
		public String beanB() {
			return "yo";
		}
	}

	private void assertThatContextHasViolations(Class<?> context, BeanOverrideViolation... violations) {
		assertThat(new BeanOverrideChecker().getBeanOverrideViolations(context), Matchers.containsInAnyOrder(violations));
	}

	@Test
	public void oneViolationTest() {
		BeanDefinition overridingBeanB = new BeanDefinition("beanB", OneViolationContext.class);
		assertThatContextHasViolations(OneViolationContext.class, new BeanOverrideViolation.NotOverriding(overridingBeanB));
	}

	@Configuration
	@Import(BaseContext.class)
	public static class TwoViolationsContext {
		@Bean
		public String beanA() {
			return "too baaad";
		}

		@Bean
		@BeanOverride
		public String beanB() {
			return "yo";
		}
	}

	@Test
	public void twoViolationTest() {
		assertThatContextHasViolations(TwoViolationsContext.class, new BeanOverrideViolation.NotOverriding(new BeanDefinition("beanB",
				TwoViolationsContext.class)), new BeanOverrideViolation.ShouldNotOverride(
				new BeanDefinition("beanA", TwoViolationsContext.class), BaseContext.class));
	}

	@Configuration
	@Import(BaseContext.class)
	public static class ThreeViolationsContext {
		@Bean
		public String beanA() {
			return "too baaad";
		}

		@Bean
		@BeanOverride
		public String beanB() {
			return "yo";
		}

		@Bean
		@BeanOverride(context = TwoViolationsContext.class)
		public String beanC() {
			return "bla";
		}
	}

	@Test
	public void threeViolationTest() {
		Assert.hasSize(new BeanOverrideChecker().getBeanOverrideViolations(ThreeViolationsContext.class), 3);
	}

}
