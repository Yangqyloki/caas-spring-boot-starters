package com.hybris.caas.security.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Provides a convenient base interface for creating {@link HttpSecurity} declarative request authorization.
 * Implementations will be applied to the default {@code WebSecurityConfigurerAdapter} to enhance the
 * existing {@link HttpSecurity} object and must be annotated with {@code @Configuration} or other Spring stereotype
 * annotation to make them spring beans.
 *
 * The alternative is to provide your own complete implementation of the standard {@code WebSecurityConfigurerAdapter}
 * for complete control over the security configuration.
 */
public interface ServiceWebSecurityConfigurerAdapter
{
	/**
	 * Override this method to configure the {@link HttpSecurity}. Typically subclasses
	 * should not invoke this method by calling super as it may override their
	 * configuration.
	 *
	 * @param http the {@link HttpSecurity} to modify
	 * @throws Exception if an error occurs with during {@link HttpSecurity#authorizeRequests()}
	 */
	@SuppressWarnings("squid:S00112") // Generic exception should never be thrown. This is to be compliant with the Spring contract.
	void configure(HttpSecurity http) throws Exception;
}
