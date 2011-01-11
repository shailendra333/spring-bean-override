package org.detailsmatter.util.spring;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class BeanOverrideCheckerMultipleImportsOfSameContextTest {
	@Configuration
	public static class UsefulContext {
		@Bean
		public String usefulBean() {
			return "rocks";
		}
	}

	@Configuration
	@Import(UsefulContext.class)
	public static class A {
	}

	@Configuration
	@Import(UsefulContext.class)
	public static class B {
	}

	@Configuration
	@Import({A.class, B.class})
	public static class AppContext {
	}

	@Test
	public void shouldNotSeeUsefulBeanTwice() {
		new BeanOverrideChecker().checkBeanOverride(AppContext.class);
	}
}
