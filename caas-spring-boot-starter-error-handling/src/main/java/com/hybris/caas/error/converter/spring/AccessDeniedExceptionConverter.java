package com.hybris.caas.error.converter.spring;


import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.springframework.http.HttpStatus;
import com.hybris.caas.error.converter.AbstractExceptionConverter;

import org.springframework.security.access.AccessDeniedException;

/**
 * Convert a {@link AccessDeniedException} to a {@link ErrorMessage}.
 */
public class AccessDeniedExceptionConverter extends AbstractExceptionConverter<AccessDeniedException>
{
	@Override
	protected ErrorMessage convert(AccessDeniedException ex)
	{
		return ErrorMessage.builder()
				.withMessage(ErrorConstants.MESSAGE_403)
				.withStatus(HttpStatus.FORBIDDEN.value())
				.withType(ErrorConstants.TYPE_403_INSUFFICIENT_PERMISSIONS)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}
}
