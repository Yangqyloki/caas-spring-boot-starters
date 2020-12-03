package com.hybris.caas.error.config;

import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.converter.spring.AccessDeniedExceptionConverter;
import com.hybris.caas.error.converter.spring.AuthenticationExceptionConverter;
import com.hybris.caas.error.converter.spring.InsufficientAuthenticationExceptionConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

@ConditionalOnWebApplication
@ConditionalOnClass(AccessDeniedException.class)
@Configuration("errorSecurityConfig")
public class ErrorSecurityConfig
{
	@Bean
	public AbstractExceptionConverter<AccessDeniedException> accessDeniedExceptionConverter()
	{
		return new AccessDeniedExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<AuthenticationException> authenticationExceptionConverter()
	{
		return new AuthenticationExceptionConverter();
	}

	@Bean
	public AbstractExceptionConverter<InsufficientAuthenticationException> insufficientAuthenticationExceptionConverter()
	{
		return new InsufficientAuthenticationExceptionConverter();
	}
}
