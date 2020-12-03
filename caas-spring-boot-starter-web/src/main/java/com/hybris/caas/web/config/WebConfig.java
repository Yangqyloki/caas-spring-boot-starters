package com.hybris.caas.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hybris.caas.error.converter.ExceptionConverter;
import com.hybris.caas.security.service.AuthorizationManager;
import com.hybris.caas.security.util.CaasCloudSecurityProperties;
import com.hybris.caas.web.batch.BatchRequestArgumentResolver;
import com.hybris.caas.web.batch.WrappedBatchRequestArgumentResolver;
import com.hybris.caas.web.converter.AuditToMetadataDtoConverter;
import com.hybris.caas.web.converter.GuidConverter;
import com.hybris.caas.web.converter.KeyMultiValuesConverter;
import com.hybris.caas.web.pagination.CaasPageableHandlerMethodArgumentResolver;
import com.hybris.caas.web.sort.CaasSortHandlerMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortArgumentResolver;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.Validator;
import java.util.List;

@Configuration
@ConditionalOnWebApplication
@ComponentScan("com.hybris.caas.web")
public class WebConfig implements WebMvcConfigurer
{
	private static final String PARAM_PAGE_NUMBER = "pageNumber";
	private static final String PARAM_PAGE_SIZE = "pageSize";
	private static final String PARAM_SORT = "sort";
	private static final String SORT_PROPERTY_DELIMITER = ":";
	private static final int DEFAULT_PAGE_NUMBER = 0;
	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int DEFAULT_MAX_PAGE_SIZE = 1_000;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private Validator validator;

	@Qualifier("exceptionConverterFactory")
	@Autowired
	private ExceptionConverter exceptionConverter;

	@Bean
	public PageableArgumentResolver pageableArgumentResolver()
	{
		final Pageable fallbackPageable = PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
		final PageableHandlerMethodArgumentResolver pageableResolver =
				new CaasPageableHandlerMethodArgumentResolver(sortArgumentResolver());

		pageableResolver.setOneIndexedParameters(true);
		pageableResolver.setPageParameterName(PARAM_PAGE_NUMBER);
		pageableResolver.setSizeParameterName(PARAM_PAGE_SIZE);
		pageableResolver.setMaxPageSize(DEFAULT_MAX_PAGE_SIZE);
		pageableResolver.setFallbackPageable(fallbackPageable);
		return pageableResolver;
	}

	@Bean
	public SortArgumentResolver sortArgumentResolver()
	{
		final Sort fallbackSort = Sort.unsorted();
		final CaasSortHandlerMethodArgumentResolver sortResolver = new CaasSortHandlerMethodArgumentResolver();
		sortResolver.setFallbackSort(fallbackSort);
		sortResolver.setSortParameter(PARAM_SORT);
		sortResolver.setPropertyDelimiter(SORT_PROPERTY_DELIMITER);
		return sortResolver;
	}

	@Bean
	public BatchRequestArgumentResolver batchRequestArgumentResolver()
	{
		return new BatchRequestArgumentResolver(mapper, validator, exceptionConverter);
	}

	@Bean
	public WrappedBatchRequestArgumentResolver wrappedBatchRequestArgumentResolver()
	{
		return new WrappedBatchRequestArgumentResolver(mapper, validator, exceptionConverter);
	}

	@Bean
	@ConditionalOnMissingBean(name = "auditToMetadataDtoConverter")
	@ConditionalOnBean({ AuthorizationManager.class, CaasCloudSecurityProperties.class })
	public AuditToMetadataDtoConverter auditToMetadataDtoConverter(final AuthorizationManager authorizationManager,
			final CaasCloudSecurityProperties caasCloudSecurityProperties)
	{
		return new AuditToMetadataDtoConverter(authorizationManager, caasCloudSecurityProperties);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers)
	{
		argumentResolvers.add(0, sortArgumentResolver());
		argumentResolvers.add(0, pageableArgumentResolver());
		argumentResolvers.add(batchRequestArgumentResolver());
		argumentResolvers.add(wrappedBatchRequestArgumentResolver());
	}

	@Override
	public void addFormatters(FormatterRegistry registry)
	{
		registry.addConverter(new GuidConverter());
		registry.addConverter(new KeyMultiValuesConverter());
	}

	/**
	 * Configures path matching by:
	 * - Disabling suffix pattern matching due to path segment identifier (i.e. product identifier) allowing dots
	 * 
	 * @deprecated to be removed when migrating to spring framework 5.3
	 */
	@Override
	public void configurePathMatch(PathMatchConfigurer configurer)
	{
		configurer.setUseSuffixPatternMatch(Boolean.FALSE);
	}

	/**
	 * Configures content negotiation by:
	 * - Disabling parameter matching
	 * - Disabling path extensions (i.e. ".html")
	 * The only other option supported will be through the use of the Accept header.
	 * 
	 * @deprecated to be removed when migrating to spring framework 5.3
	 */
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer)
	{
		configurer.defaultContentType(MediaType.APPLICATION_JSON);

		configurer.favorParameter(false);
		configurer.favorPathExtension(false);
	}

}