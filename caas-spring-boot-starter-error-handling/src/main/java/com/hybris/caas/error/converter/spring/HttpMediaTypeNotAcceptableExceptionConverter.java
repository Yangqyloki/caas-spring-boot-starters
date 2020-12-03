package com.hybris.caas.error.converter.spring;

import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotAcceptableException;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;

/**
 * Convert a {@link HttpMediaTypeNotAcceptableException} to a
 * {@link ErrorMessage}.
 */
public class HttpMediaTypeNotAcceptableExceptionConverter extends AbstractExceptionConverter<HttpMediaTypeNotAcceptableException>
{

	@Override
	protected ErrorMessage convert(HttpMediaTypeNotAcceptableException ex)
	{
		return ErrorMessage.builder()
				.withMessage(ex.getMessage())
				.withStatus(HttpStatus.NOT_ACCEPTABLE.value())
				.withType(ErrorConstants.TYPE_406_UNSUPPORTED_RESPONSE_CONTENT_TYPE)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}

}