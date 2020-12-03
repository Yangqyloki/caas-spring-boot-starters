package com.hybris.caas.error.converter;

import com.hybris.caas.error.ErrorMessage;

/**
 * Abstract base class for all concrete {@link ExceptionConverter} instances.
 *
 * @param <T>
 *            Exception type handled by this converter.
 */
public abstract class AbstractExceptionConverter<T extends Throwable> implements ExceptionConverter
{

	@SuppressWarnings("unchecked")
	@Override
	public ErrorMessage toErrorMessage(Throwable ex)
	{
		return convert((T) ex);
	}

	/**
	 * Generics-based conversion method.
	 *
	 * @param ex
	 *            - the exception to convert
	 * @return the error message
	 */
	protected abstract ErrorMessage convert(T ex);
}
