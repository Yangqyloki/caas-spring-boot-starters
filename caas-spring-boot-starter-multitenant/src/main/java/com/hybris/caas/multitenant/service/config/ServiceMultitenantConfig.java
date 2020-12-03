package com.hybris.caas.multitenant.service.config;

import com.hybris.caas.multitenant.service.SapJwtTenantService;
import com.hybris.caas.multitenant.service.TenantService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Provides configuration for service multitenant support.
 */
@Configuration
@EnableConfigurationProperties(TenantProperties.class)
public class ServiceMultitenantConfig
{
	@Configuration
	@Profile("dev")
	@PropertySource("classpath:/tenant-dev.properties")
	public static class DevTenantConfig
	{
		// empty
	}

	@Configuration
	@Profile("test")
	@PropertySource("classpath:/tenant-test.properties")
	public static class TestTenantConfig
	{
		// empty
	}

	@Configuration
	@Profile("stage")
	@PropertySource("classpath:/tenant-stage.properties")
	public static class StageTenantConfig
	{
		// empty
	}

	@Configuration
	@Profile("prod")
	@PropertySource("classpath:/tenant-prod.properties")
	public static class ProdTenantConfig
	{
		// empty
	}

	@Configuration
	@Profile("prod-euc1")
	@PropertySource("classpath:/tenant-prod-euc1.properties")
	public static class ProdEuC1TenantConfig
	{
		// empty
	}

	@Configuration
	@Profile("prod-euw2")
	@PropertySource("classpath:/tenant-prod-euw2.properties")
	public static class ProdEuW2TenantConfig
	{
		// empty
	}

	@Bean
	public TenantProperties tenantProperties()
	{
		return new TenantProperties();
	}

	@Bean
	@ConditionalOnMissingBean(TenantService.class)
	public TenantService tenantService()
	{
		return new SapJwtTenantService(tenantProperties());
	}
}
