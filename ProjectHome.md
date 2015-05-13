Specify and check bean overrides with annotations.



# Getting started #

Let's create an application context App with a bean, _bean1_ that will get overridden by another context OverrideApp.

To define the override, use the @BeanOverride annotation on the overriding bean.

```
@Configuration
public static class App {
	@Bean
	public String bean1() {
		return "bean1";
	}
}

@Configuration
@Import(App.class)
public static class OverrideApp {
	@Bean
	@BeanOverride
	public String bean1() {
		return "override bean1";
	}
}

new BeanOverrideChecker().checkBeanOverride(OverrideApp.class);
```


# More accuracy #

Now we'd like to not only specify that a bean overrides another bean, but also exactly which context is targeted :

```
	@Bean
	@BeanOverride(context = App.class)
	public String bean1() {
		return "override bean1";
	}
```


# Override happening outside of the bean definition #

What if I have two unrelated context :

```
@Configuration
public static class AppContext {
	@Bean
	public String aBean() {
		return "cool";
	}
}

@Configuration
public static class TestContext {
	@Bean
	public String aBean() {
		return "override cool";
	}
}
```

and a third that uses them :

```
@Configuration
@Import({AppContext.class, TestContext.class})
public static class AppTestContext {
}
```

This is gonna break as TestContext.aBean will override AppContext.aBean.

We could change TestContext, introduce a dependency to AppContext and add a @BeanOverride. Something like :

```
@Configuration
@Import(AppContext.class)
public static class TestContext {
	@Bean
	@BeanOverride(AppContext.class)
	public String aBean() {
		return "override cool";
	}
}
```


But that's not what we want. Maybe TestContext is defined in an artifact that has no dependency on AppContext. Also what if we want to use TestContext to override a bean coming from another context, not AppContext.

The idea of course is to define the override in the top level context, in AppTestContext. We do it with @ContextOverride annotation :

```
@Configuration
@Import({AppContext.class, TestContext.class})
@ContextOverride(beans = "aBean", of = AppContext.class, with = TestContext.class)
public static class AppTestContext {
}
```

## Overriding multiple beans, from different contexts ##

Let's imagine AppContext and TestContext have both bBean we can define both override in the same declaration :

```
@ContextOverride(beans = {"aBean","bBean"}, of = AppContext.class, with = TestContext.class)
```

What if there are multiple context involved :

```
@ContextOverrides({
		@ContextOverride(beans = {"aBean", "bBean"}, of = AppContext.class, with = TestContext.class),
		@ContextOverride(beans = "cBean", of = OtherContext.class, with = TestContext.class)})
```

# Enforcing Overrides Checks #

## At Runtime ##

Use the _BeanOverrideCheckerPostProcessor_ :

```
@Configuration
@Import(BeanOverrideCheckerPostProcessor.Context.class)
public class MyContext {

}
```

## At Compile-time ##

TBD ...