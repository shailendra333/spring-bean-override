package org.detailsmatter.util.spring;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class BeanOverrideCheckerInvalidOverrideTest {
	@Configuration
	public static class App {
		@Bean
		public String bean1() {
			return "bean1";
		}
	}

	@Configuration
	@Import(App.class)
	public static class MissingOverrideApp {
		@Bean
		public String bean1() {
			return "override bean1";
		}
	}

	@Test(expected = IllegalBeanOverrideException.class)
	public void unspecifiedOverrideShouldFail() {
		new BeanOverrideChecker().checkBeanOverride(MissingOverrideApp.class);
	}

	@Configuration
	@Import(App.class)
	public static class WrongOverrideApp {
		@Bean
		@BeanOverride(context = MissingOverrideApp.class)
		public String bean1() {
			return "override bean1";
		}
	}

	@Test(expected = IllegalBeanOverrideException.class)
	public void wrongOverrideTargetShouldFail() {
		// is overriding App but was expected to override MissingOverrideApp
		new BeanOverrideChecker().checkBeanOverride(WrongOverrideApp.class);
	}

	@Configuration
	@Import(WrongOverrideOrderOverridingApp.class)
	public static class WrongOverrideOrderApp {
		@Bean
		public String bean1() {
			return "bean1";
		}
	}

	// the @Import should be on this class, not on WrongOverrideOrderApp
	@Configuration
	public static class WrongOverrideOrderOverridingApp {
		@Bean
		@BeanOverride
		public String bean1() {
			return "override bean1";
		}
	}

	@Test(expected = IllegalBeanOverrideException.class)
	public void wrongOverrideOrderShouldBeDetected() {
		new BeanOverrideChecker().checkBeanOverride(WrongOverrideOrderApp.class);

	}

}
