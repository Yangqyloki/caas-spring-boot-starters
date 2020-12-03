package com.hybris.caas.error.converter.custom;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.exception.TooManyRequestsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * Convert a {@link TooManyRequestsException} to a {@link ErrorMessage} and sets the time to wait until the next attempt in the
 * {@code Retry-After} response header.
 */
public class TooManyRequestsExceptionConverter extends AbstractExceptionConverter<TooManyRequestsException>
{
	@Override
	protected ErrorMessage convert(final TooManyRequestsException ex)
	{
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Retry-After", Objects.toString(ex.getRetryAfter()));

		return ErrorMessage.builder()
				.withMessage(ex.getMessage())
				.withStatus(HttpStatus.TOO_MANY_REQUESTS.value())
				.withType(ErrorConstants.TYPE_429_TOO_MANY_REQUESTS)
				.withMoreInfo(ErrorConstants.INFO)
				.withResponseHeaders(httpHeaders)
				.build();
	}
}
