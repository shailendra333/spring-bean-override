package org.detailsmatter.util.spring;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class BeanOverrideCheckerPostProcessorTest {
	@Configuration
	public static class BaseContext {
		@Bean
		public String aBean() {
			return "baseBean";
		}
	}

	@Configuration
	@Import(BaseContext.class)
	public static class IncorrectAppContext {
		@Bean
		public String aBean() {
			return "overriding bean";
		}
	}

	@Configuration
	@Import(BaseContext.class)
	public static class CorrectAppContext {
		@Bean
		@BeanOverride
		public String aBean() {
			return "overriding bean";
		}
	}

	@Test
	public void postProcessorShouldFailWhenIncorrectOverride() {
		try {
		new AnnotationConfigApplicationContext( //
				IncorrectAppContext.class, BeanOverrideCheckerPostProcessor.Context.class);
			Assert.fail("Should throw IllegalBeanOverride");
		} catch( Throwable t ) {
			Assert.assertNotSame(-1, ExceptionUtils.indexOfThrowable(t, IllegalBeanOverrideException.class));
		}
	}

	@Test
	public void shouldWorkWhenIncorrectOverrideWithoutPostProcessorCheck() {
		new AnnotationConfigApplicationContext(IncorrectAppContext.class);
	}

	@Test
	public void shouldWorkWhenCorrectOverrideWithPostProcessorCheck() {
		new AnnotationConfigApplicationContext(//
				CorrectAppContext.class, BeanOverrideCheckerPostProcessor.Context.class);
	}
}
