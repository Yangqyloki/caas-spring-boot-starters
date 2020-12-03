package com.hybris.caas.web.config;

import com.hybris.caas.web.i18n.CaasLocaleResolver;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoConfigurationIntegrationTest
{
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
			AutoConfigurations.of(I18nConfig.class));

	@Test
	public void should_get_autoconfigured_LocaleResolver()
	{
		contextRunner.withPropertyValues("caas.i18n.locale-resolver.enabled:true").run((context) -> {
			assertThat(context).hasSingleBean(LocaleResolver.class);
			assertThat(context).getBean(LocaleResolver.class).isInstanceOf(CaasLocaleResolver.class);
		});
	}

	@Test
	public void should_not_get_autoconfigured_LocaleResolver()
	{
		contextRunner.withPropertyValues("caas.i18n.locale-resolver.enabled:false")
				.run((context) -> assertThat(context).doesNotHaveBean(LocaleResolver.class));
	}

	@Test
	public void should_get_overridden_LocaleResolver()
	{
		contextRunner.withUserConfiguration(TestConfig.class).run((context) -> {
			assertThat(context).hasSingleBean(LocaleResolver.class);
			assertThat(context).getBean(LocaleResolver.class).isInstanceOf(AcceptHeaderLocaleResolver.class);
		});
	}

	@Configuration
	static class TestConfig
	{
		@Bean
		public LocaleResolver localeResolver()
		{
			return new AcceptHeaderLocaleResolver();
		}
	}
}
