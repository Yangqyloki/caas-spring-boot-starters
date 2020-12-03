package com.hybris.caas.security.config;

import com.sap.cloud.security.xsuaa.XsuaaServiceConfiguration;
import com.sap.cloud.security.xsuaa.token.TokenAuthenticationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * CaaS web security configuration adapter will set the following automatically for you:
 * - Stateless session management
 * - OAuth2 resource server with JWT authentication provider
 * - Custom authentication entry point
 * - Custom access denied handler
 * - Sets frame options to "same origin" in HTTP response header
 * - Disable CSRF
 * - Register CORS request filter
 *
 * To extend this configuration, services must provide beans of type {@link ServiceWebSecurityConfigurerAdapter}.
 *
 * This configuration can be disabled by setting {@code caas.security.web.enabled=false}.
 */
public class CaasWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter implements Ordered
{
	private static final Logger LOG = LoggerFactory.getLogger(CaasWebSecurityConfigurerAdapter.class);
	private static final int ORDER = 0;

	final XsuaaServiceConfiguration xsuaaServiceConfiguration;
	final AuthenticationEntryPoint authenticationEntryPoint;
	final AccessDeniedHandler accessDeniedHandler;
	final List<ServiceWebSecurityConfigurerAdapter> webSecurityConfigurerAdapters;

	public CaasWebSecurityConfigurerAdapter(final XsuaaServiceConfiguration xsuaaServiceConfiguration,
			final AuthenticationEntryPoint authenticationEntryPoint, final AccessDeniedHandler accessDeniedHandler,
			final List<ServiceWebSecurityConfigurerAdapter> webSecurityConfigurerAdapters)
	{
		this.xsuaaServiceConfiguration = xsuaaServiceConfiguration;
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.accessDeniedHandler = accessDeniedHandler;
		this.webSecurityConfigurerAdapters = Optional.ofNullable(webSecurityConfigurerAdapters).orElseGet(ArrayList::new);
	}

	// @formatter:off
	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
		.and()
				.oauth2ResourceServer()
				.jwt()
				.jwtAuthenticationConverter(getJwtAuthoritiesConverter(this.xsuaaServiceConfiguration));

		http.headers().frameOptions().sameOrigin();
		http.csrf().disable();
		http.cors();
		LOG.info("CaaS Security - enabled XSUAA OAuth2 resource server configuration");

		if (Objects.nonNull(authenticationEntryPoint))
		{
			http.oauth2ResourceServer().authenticationEntryPoint(authenticationEntryPoint);
			http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
			LOG.info("CaaS Security - registered custom authentication entry point");
		}
		if (Objects.nonNull(accessDeniedHandler))
		{
			http.oauth2ResourceServer().accessDeniedHandler(accessDeniedHandler);
			http.exceptionHandling().accessDeniedHandler(accessDeniedHandler);
			LOG.info("CaaS Security - registered custom access denied handler");
		}
		for (final ServiceWebSecurityConfigurerAdapter adapter : webSecurityConfigurerAdapters)
		{
			adapter.configure(http);
		}
		LOG.info("CaaS Security - applied {} ServiceWebSecurityConfigurerAdapters", webSecurityConfigurerAdapters.size());
	}
	// @formatter:on

	/**
	 * Converter to be used by XSUAA.
	 *
	 * @see "https://github.com/SAP/cloud-security-xsuaa-integration/tree/master/spring-xsuaa"
	 * @param xsuaaServiceConfiguration the xsuaa configuration
	 * @return the converter
	 */
	protected Converter<Jwt, AbstractAuthenticationToken> getJwtAuthoritiesConverter(
			final XsuaaServiceConfiguration xsuaaServiceConfiguration)
	{
		final TokenAuthenticationConverter converter = new TokenAuthenticationConverter(xsuaaServiceConfiguration);
		converter.setLocalScopeAsAuthorities(true); // not applicable in case of multiple xsuaa bindings!
		return converter;
	}

	@Override
	public int getOrder()
	{
		return ORDER;
	}
}