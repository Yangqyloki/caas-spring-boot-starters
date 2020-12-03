package com.hybris.caas.log.config;

import brave.Tracing;
import com.hybris.caas.log.context.TracingUserProvider;
import com.hybris.caas.log.context.UserProvider;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoConfigurationIntegrationTest
{

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(TraceAutoConfiguration.class, AuditLogConfig.class));

	@Test
	public void should_get_autoconfigured_UserProvider()
	{
		contextRunner.run((context) -> {
			assertThat(context).hasSingleBean(UserProvider.class);
			assertThat(context).getBean(UserProvider.class).isInstanceOf(TracingUserProvider.class);
		});
	}

	@Test
	public void should_get_overridden_UserProvider()
	{
		contextRunner
				.withUserConfiguration(TestConfig.class)
				.run((context) -> {
					assertThat(context).hasSingleBean(UserProvider.class);
					assertThat(context).getBean(UserProvider.class).isInstanceOf(TracingUserProvider.class);
				});
	}

	@Configuration
	static class TestConfig
	{

		@Bean
		public UserProvider overridingUserProvider(final Tracing tracing)
		{
			return new OverriddenUserProvider(tracing);
		}

		public static class OverriddenUserProvider extends TracingUserProvider
		{
			public OverriddenUserProvider(Tracing tracing)
			{
				super(tracing);
			}
		}

	}
}
