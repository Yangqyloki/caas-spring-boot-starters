package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;

/**
 * Convert a {@link HttpMediaTypeNotSupportedException} to a {@link ErrorMessage} and sets the supported media types in the
 * {@code Accept} response header.
 */
public class HttpMediaTypeNotSupportedExceptionConverter extends AbstractExceptionConverter<HttpMediaTypeNotSupportedException>
{

	@Override
	protected ErrorMessage convert(HttpMediaTypeNotSupportedException ex)
	{
		final HttpHeaders headers = new HttpHeaders();
		headers.setAccept(ex.getSupportedMediaTypes());

		return ErrorMessage.builder()
				.withMessage(ex.getMessage())
				.withStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
				.withType(ErrorConstants.TYPE_415_UNSUPPORTED_REQUEST_CONTENT_TYPE)
				.withMoreInfo(ErrorConstants.INFO)
				.withResponseHeaders(headers)
				.build();
	}

}
