package org.detailsmatter.util.spring;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class BeanOverrideCheckerContextOverridesTest {
	@Configuration
	public static class AppContext {
		@Bean
		public String aBean() {
			return "cool";
		}

		@Bean
		public String bBean() {
			return "second cool";
		}

		@Bean
		public String cBean() {
			return "not overridden";
		}
	}

	@Configuration
	public static class TestContext {
		@Bean
		public String aBean() {
			return "override cool";
		}

		@Bean
		public String bBean() {
			return "override second cool";
		}

		@Bean
		public String dBean() {
			return "a bean";
		}
	}

	@Configuration
	@Import({AppContext.class, TestContext.class})
	@ContextOverride(beans = {"aBean", "bBean"}, of = AppContext.class, with = TestContext.class)
	public static class AppTestContext {
	}

	@Test
	public void overrideShouldWork() {
		new BeanOverrideChecker().checkBeanOverride(AppTestContext.class);
	}

	@Configuration
	@Import({AppContext.class, TestContext.class})
	@ContextOverrides({@ContextOverride(beans = "aBean", of = AppContext.class, with = TestContext.class),
			@ContextOverride(beans = {"bBean", "cBean"}, of = AppContext.class, with = TestContext.class)})
	public static class TooManyOverrideAppTestContext {
	}

	@Test(expected = IllegalBeanOverrideException.class)
	public void tooManyOverrides() {
		new BeanOverrideChecker().checkBeanOverride(TooManyOverrideAppTestContext.class);
	}

	@Configuration
	@Import({AppContext.class, TestContext.class})
	@ContextOverride(beans = "aBean", of = AppContext.class, with = TestContext.class)
	public static class MissingOverrideAppTestContext {
	}

	@Test(expected = IllegalBeanOverrideException.class)
	public void missingOverrides() {
		new BeanOverrideChecker().checkBeanOverride(MissingOverrideAppTestContext.class);
	}

}
