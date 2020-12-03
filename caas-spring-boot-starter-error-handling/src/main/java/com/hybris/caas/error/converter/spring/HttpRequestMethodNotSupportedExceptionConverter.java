package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;

/**
 * Convert a {@link HttpRequestMethodNotSupportedException} to a
 * {@link ErrorMessage}.
 */
public class HttpRequestMethodNotSupportedExceptionConverter extends AbstractExceptionConverter<HttpRequestMethodNotSupportedException>
{

	@Override
	protected ErrorMessage convert(HttpRequestMethodNotSupportedException ex)
	{
		final HttpHeaders headers = new HttpHeaders();
		headers.setAllow(ex.getSupportedHttpMethods());

		return ErrorMessage.builder()
				.withMessage(ex.getMessage())
				.withStatus(HttpStatus.METHOD_NOT_ALLOWED.value())
				.withType(ErrorConstants.TYPE_405_UNSUPPORTED_METHOD)
				.withMoreInfo(ErrorConstants.INFO)
				.withResponseHeaders(headers)
				.build();
	}

}
