package com.hybris.caas.error.converter.javax;

import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractCauseExceptionConverter;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * Convert a {@link UndeclaredThrowableException} to a {@link ErrorMessage}.
 */
public class UndeclaredThrowableExceptionConverter extends AbstractCauseExceptionConverter<UndeclaredThrowableException>
{
	@Override
	protected boolean useRootCause()
	{
		return false;
	}
}
