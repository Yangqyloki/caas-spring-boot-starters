package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;

/**
 * Convert a {@link InsufficientAuthenticationException} to a {@link ErrorMessage}.
 */
public class InsufficientAuthenticationExceptionConverter extends AbstractExceptionConverter<InsufficientAuthenticationException>
{
	@Override
	protected ErrorMessage convert(final InsufficientAuthenticationException ex)
	{
		return ErrorMessage.builder()
				.withMessage(ErrorConstants.MESSAGE_401)
				.withStatus(HttpStatus.UNAUTHORIZED.value())
				.withType(ErrorConstants.TYPE_401_INSUFFICIENT_CREDENTIALS)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}
}
