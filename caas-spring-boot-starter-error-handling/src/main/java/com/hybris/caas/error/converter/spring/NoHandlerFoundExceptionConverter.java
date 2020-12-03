package com.hybris.caas.error.converter.spring;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;

/**
 * Convert a {@link NoHandlerFoundException} to a
 * {@link ErrorMessage}.
 */
public class NoHandlerFoundExceptionConverter extends AbstractExceptionConverter<NoHandlerFoundException>
{

	@Override
	protected ErrorMessage convert(NoHandlerFoundException ex)
	{
		return ErrorMessage.builder()
				.withMessage(ex.getMessage())
				.withStatus(HttpStatus.NOT_FOUND.value())
				.withType(ErrorConstants.TYPE_404_ELEMENT_RESOURCE_NOT_EXISTING)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}

}
