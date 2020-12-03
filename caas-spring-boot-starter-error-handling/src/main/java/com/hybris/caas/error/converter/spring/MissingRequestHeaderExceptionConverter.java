package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;

/**
 * Convert a {@link MissingRequestHeaderException} to a {@link ErrorMessage}.
 */
public class MissingRequestHeaderExceptionConverter extends AbstractExceptionConverter<MissingRequestHeaderException>
{
	protected static final String EXCEPTION_MESSAGE = "Missing header: ";

	@Override
	protected ErrorMessage convert(MissingRequestHeaderException ex)
	{
		return ErrorMessage.builder()
				.withMessage(EXCEPTION_MESSAGE + ex.getHeaderName())
				.withStatus(HttpStatus.PRECONDITION_FAILED.value())
				.withType(ErrorConstants.TYPE_412_PRECONDITION_FAILED)
				.build();
	}
}
