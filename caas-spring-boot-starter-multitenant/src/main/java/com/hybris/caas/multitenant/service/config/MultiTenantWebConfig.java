package com.hybris.caas.multitenant.service.config;

import brave.Tracing;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.multitenant.service.TenantService;
import com.hybris.caas.multitenant.service.exception.InvalidTenantException;
import com.hybris.caas.multitenant.service.exception.InvalidTenantFormatException;
import com.hybris.caas.multitenant.service.exception.MissingTenantException;
import com.hybris.caas.multitenant.service.exception.converter.InvalidTenantExceptionConverter;
import com.hybris.caas.multitenant.service.exception.converter.InvalidTenantFormatExceptionConverter;
import com.hybris.caas.multitenant.service.exception.converter.MissingTenantExceptionConverter;
import com.hybris.caas.multitenant.web.TenantArgumentResolver;
import com.hybris.caas.multitenant.web.TenantHolderArgumentResolver;
import com.hybris.caas.multitenant.web.TenantTracingPropagationFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.util.List;

/**
 * Provides configuration for web multitenant support.
 */
@Configuration
@ComponentScan(basePackages = "com.hybris.caas.multitenant")
@AutoConfigureAfter(TraceAutoConfiguration.class)
public class MultiTenantWebConfig implements WebMvcConfigurer
{
	private TenantService tenantService;

	public MultiTenantWebConfig(final TenantService tenantService)
	{
		this.tenantService = tenantService;
	}

	@Bean
	public AbstractExceptionConverter<MissingTenantException> missingTenantExceptionConverter()
	{
		return new MissingTenantExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<InvalidTenantException> invalidTenantExceptionConverter()
	{
		return new InvalidTenantExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<InvalidTenantFormatException> invalidTenantFormatExceptionAbstractExceptionConverter()
	{
		return new InvalidTenantFormatExceptionConverter();
	}

	@Bean
	@ConditionalOnBean(Tracing.class)
	public Filter tenantTracingPropagationFilter(final Tracing tracing)
	{
		return new TenantTracingPropagationFilter(tracing, tenantService);
	}

	@Bean
	public HandlerMethodArgumentResolver tenantHolderArgumentResolver()
	{
		return new TenantHolderArgumentResolver(tenantService);
	}

	@Bean
	public HandlerMethodArgumentResolver tenantArgumentResolver()
	{
		return new TenantArgumentResolver(tenantService);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers)
	{
		argumentResolvers.add(tenantHolderArgumentResolver());
		argumentResolvers.add(tenantArgumentResolver());
	}
}
