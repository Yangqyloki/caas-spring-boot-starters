package com.hybris.caas.error.converter.spring.db;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;

/**
 * Converts {@link EmptyResultDataAccessException} to canonical error message.
 */
public class EmptyResultDataAccessExceptionConverter extends AbstractExceptionConverter<EmptyResultDataAccessException>
{
	private static final String MESSAGE = "The resource does not exist.";

	@Override
	protected ErrorMessage convert(EmptyResultDataAccessException ex)
	{
		return ErrorMessage.builder()
				.withMessage(MESSAGE)
				.withStatus(HttpStatus.NOT_FOUND.value())
				.withType(ErrorConstants.TYPE_404_ELEMENT_RESOURCE_NOT_EXISTING)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}
}
