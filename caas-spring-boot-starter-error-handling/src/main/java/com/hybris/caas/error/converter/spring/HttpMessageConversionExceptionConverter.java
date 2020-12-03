package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractCauseExceptionConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;

/**
 * Convert a {@link HttpMessageConversionException} to a {@link ErrorMessage}.
 */
public class HttpMessageConversionExceptionConverter extends AbstractCauseExceptionConverter<HttpMessageConversionException>
{
	protected static final String EXCEPTION_MESSAGE = "Request body is missing or malformed.";

	@Override
	protected ErrorMessage convert(HttpMessageConversionException ex)
	{
		return ErrorMessage.builder()
				.withMessage(EXCEPTION_MESSAGE)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}

	@Override
	protected boolean useRootCause()
	{
		return false;
	}
}
