package com.hybris.caas.multitenant.service.config;

import com.hybris.caas.multitenant.service.SapJwtTenantService;
import com.hybris.caas.multitenant.service.TenantService;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoConfigurationIntegrationTest
{

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
			AutoConfigurations.of(ServiceMultitenantConfig.class))
			.withPropertyValues("tenant.forwardedHostRegex:abc", "tenant.paasName:cde");

	@Test
	public void should_get_autoconfigured_TenantService()
	{
		contextRunner.run((context) -> {
			assertThat(context).hasSingleBean(TenantService.class);
			assertThat(context).getBean(TenantService.class).isInstanceOf(SapJwtTenantService.class);
		});
	}

	@Test
	public void should_get_overridden_TenantService()
	{
		contextRunner.withUserConfiguration(TestConfig.class).run((context) -> {
			assertThat(context).hasSingleBean(TenantService.class);
			assertThat(context).getBean(TenantService.class).isInstanceOf(TestConfig.OverriddenTenantService.class);
		});
	}

	@Configuration
	static class TestConfig
	{

		@Bean
		public TenantService overridingTenantService(final TenantProperties tenantProperties)
		{
			return new OverriddenTenantService(tenantProperties);
		}

		public static class OverriddenTenantService extends SapJwtTenantService
		{
			public OverriddenTenantService(final TenantProperties tenantProperties)
			{
				super(tenantProperties);
			}
		}

	}
}
