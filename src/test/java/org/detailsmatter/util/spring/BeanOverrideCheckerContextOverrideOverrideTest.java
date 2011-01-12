package org.detailsmatter.util.spring;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Check the override of {@link ContextOverride} definitions
 * @author <a href="mailto:biethb@gmail.com">Bruno Bieth</a>
 */
public class BeanOverrideCheckerContextOverrideOverrideTest {
	@Configuration
	public static class BaseContext {
		@Bean
		public String aBean() {
			return "aBean";
		}
	}

	@Configuration
	public static class NewBean {
		@Bean
		public String aBean() {
			return "new Bean";
		}
	}

	@Configuration
	public static class InBetweenBean {
		@Bean
		@BeanOverride(context = BaseContext.class)
		public String aBean() {
			return "latest Bean";
		}
	}

	@Configuration
	@Import({BaseContext.class, NewBean.class})
	@ContextOverride(beans = "aBean", of = BaseContext.class, with = NewBean.class)
	public static class OverrideABean {

	}

	@Configuration
	@Import({BaseContext.class, InBetweenBean.class, OverrideABean.class})
	@ContextOverride(beans = "aBean", of = InBetweenBean.class, with = NewBean.class)
	public static class ContextOverrideOverride {

	}

	@Test
	public void shouldBeAbleToOverrideContextOverride() {
		new BeanOverrideChecker().checkBeanOverride(ContextOverrideOverride.class);
	}
}
