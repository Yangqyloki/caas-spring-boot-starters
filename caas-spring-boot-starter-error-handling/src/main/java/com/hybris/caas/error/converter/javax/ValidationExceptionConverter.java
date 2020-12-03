package com.hybris.caas.error.converter.javax;

import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractCauseExceptionConverter;

import javax.validation.ValidationException;

/**
 * Convert a {@link ValidationException} to a {@link ErrorMessage}.
 * This will examine the root cause to try to handle an unexpected error that occurred during validation. If no root cause
 * is found, then the default conversion process will be applied, which will return a generic 500 error message.
 * <p>
 * This will allow the possibility to throw custom exceptions during the validation process and handle those exceptions
 * explicitly.
 */
public class ValidationExceptionConverter extends AbstractCauseExceptionConverter<ValidationException>
{
	@Override
	protected boolean useRootCause()
	{
		return true;
	}
}
