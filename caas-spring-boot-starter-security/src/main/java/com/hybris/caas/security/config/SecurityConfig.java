package com.hybris.caas.security.config;

import brave.Tracing;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hybris.caas.error.converter.ExceptionConverter;
import com.hybris.caas.security.sanitization.HtmlPolicyFactory;
import com.hybris.caas.security.service.AuthorizationManager;
import com.hybris.caas.security.service.DefaultAuthorizationManager;
import com.hybris.caas.security.util.CaasCloudSecurityProperties;
import com.hybris.caas.security.web.UserTracingPropagationFilter;
import com.sap.cloud.security.xsuaa.XsuaaServiceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(TraceAutoConfiguration.class)
public class SecurityConfig
{
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Qualifier("exceptionConverterFactory")
	@Autowired
	private ExceptionConverter exceptionConverter;

	@Bean
	public HtmlPolicyFactory htmlPolicyFactory()
	{
		return new HtmlPolicyFactory();
	}

	@Bean
	public AccessDeniedHandler customAccessDeniedHandler()
	{
		return (request, response, accessDeniedException) -> {
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.getOutputStream()
					.println(MAPPER.writerWithDefaultPrettyPrinter()
							.writeValueAsString(exceptionConverter.toErrorMessage(accessDeniedException)));
		};
	}

	@Bean
	public AuthenticationEntryPoint customAuthenticationEntryPoint()
	{
		return (request, response, authException) -> {
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.getOutputStream()
					.println(MAPPER.writerWithDefaultPrettyPrinter()
							.writeValueAsString(exceptionConverter.toErrorMessage(authException)));
		};
	}

	// Enable CORS for all endpoints and for all HTTP verbs.
	@Bean
	public WebMvcConfigurer corsConfigurer()
	{
		return new WebMvcConfigurer()
		{
			@Override
			public void addCorsMappings(CorsRegistry registry)
			{
				registry.addMapping("/**")
						.allowCredentials(true)
						.exposedHeaders(HttpHeaders.LOCATION, HttpHeaders.CONTENT_DISPOSITION)
						.allowedMethods(GET.name(), PUT.name(), POST.name(), PATCH.name(), DELETE.name(), HEAD.name(), OPTIONS.name());
			}
		};
	}

	@Bean
	public AuthorizationManager authorizationManager()
	{
		return new DefaultAuthorizationManager();
	}

	@Bean
	@ConditionalOnBean(Tracing.class)
	public Filter userTracingPropagationFilter(final Tracing tracing)
	{
		return new UserTracingPropagationFilter(tracing);
	}

	@Bean
	@ConditionalOnProperty(name = "caas.security.web.enabled", havingValue = "true", matchIfMissing = true)
	public WebSecurityConfigurerAdapter webSecurityConfigurerAdapter(final XsuaaServiceConfiguration xsuaaServiceConfiguration,
			@Autowired(required = false) final List<ServiceWebSecurityConfigurerAdapter> webSecurityConfigurerAdapters)
	{
		return new CaasWebSecurityConfigurerAdapter(xsuaaServiceConfiguration, customAuthenticationEntryPoint(),
				customAccessDeniedHandler(), webSecurityConfigurerAdapters);
	}

	@Bean
	@ConditionalOnMissingBean(name = "cloudSecurityProperties")
	@ConditionalOnProperty(prefix = "cloud.security", name = { "xsappname", "tenant-regex", "manage-scope" })
	@ConfigurationProperties(prefix = "cloud.security")
	public CaasCloudSecurityProperties cloudSecurityProperties()
	{
		return new CaasCloudSecurityProperties();
	}
}
