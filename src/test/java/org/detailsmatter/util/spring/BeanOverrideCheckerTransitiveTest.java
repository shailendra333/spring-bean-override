package org.detailsmatter.util.spring;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class BeanOverrideCheckerTransitiveTest {

	@Configuration
	public static class A {
		@Bean
		public String bean() {
			return "bean";
		}
	}

	@Configuration
	@Import(A.class)
	public static class B {
		@Bean
		public String otherBean() {
			return "other bean";
		}
	}

	@Configuration
	@Import(B.class)
	public static class C {
		@Bean
		public String bean() {
			return "override bean";
		}
	}

	@Test(expected = IllegalBeanOverrideException.class)
	public void missingTransitiveOverrideShouldFail() {
		new BeanOverrideChecker().checkBeanOverride(C.class);
	}

	@Configuration
	@Import(B.class)
	public static class WorkingC {
		@Bean
		@BeanOverride
		public String bean() {
			return "override bean";
		}
	}

	@Test
	public void transitiveOverrideShouldWork() {
		new BeanOverrideChecker().checkBeanOverride(WorkingC.class);
	}
}
