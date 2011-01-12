package org.detailsmatter.util.spring;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class BeanOverrideCheckerTransitiveContextOverridesTest {
	@Configuration
	public static class TwoBeans {
		@Bean
		public String beanA() {
			return "beanA";
		}

		@Bean
		public String beanB() {
			return "beanB";
		}
	}

	@Configuration
	public static class AnotherSameTwoBeans {
		@Bean
		public String beanA() {
			return "another beanA";
		}

		@Bean
		public String beanB() {
			return "another beanB";
		}
	}

	@Configuration
	@Import(AnotherSameTwoBeans.class)
	public static class Intermediate {

	}

	@Configuration
	@Import({TwoBeans.class, Intermediate.class})
	@ContextOverride(beans = {"beanA", "beanB"}, of = TwoBeans.class, with = AnotherSameTwoBeans.class)
	public static class AnOverrideContext {

	}

	@Configuration
	@Import({TwoBeans.class, Intermediate.class, AnOverrideContext.class})
	public static class TransitiveOverride {

	}

	@Test
	public void testTransitiveContextOverride() {
		new BeanOverrideChecker().checkBeanOverride(TransitiveOverride.class);
	}
}
