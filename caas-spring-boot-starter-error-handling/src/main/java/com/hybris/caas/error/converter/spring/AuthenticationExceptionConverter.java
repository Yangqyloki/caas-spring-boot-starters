package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

/**
 * Convert a {@link AuthenticationException} to a {@link ErrorMessage}.
 */
public class AuthenticationExceptionConverter extends AbstractExceptionConverter<AuthenticationException>
{
	@Override
	protected ErrorMessage convert(final AuthenticationException ex)
	{
		return ErrorMessage.builder()
				.withMessage(ErrorConstants.MESSAGE_401)
				.withStatus(HttpStatus.UNAUTHORIZED.value())
				.withType(ErrorConstants.TYPE_401_INSUFFICIENT_CREDENTIALS)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}
}
