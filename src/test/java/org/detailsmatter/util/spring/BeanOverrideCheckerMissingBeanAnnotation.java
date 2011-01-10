package org.detailsmatter.util.spring;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class BeanOverrideCheckerMissingBeanAnnotation {
	@Configuration
	public static class App {
		@Bean
		public String bean() {
			return "";
		}
	}

	@Configuration
	@Import(App.class)
	public static class OverrideApp {
		@BeanOverride
		public String bean() {
			return "override";
		}
	}

	@Test(expected = IllegalBeanOverrideException.class)
	public void beanOverrideAnnotationMustBeUsedTogetherWithBeanAnnotation() {
		new BeanOverrideChecker().checkBeanOverride(OverrideApp.class);
	}
}
