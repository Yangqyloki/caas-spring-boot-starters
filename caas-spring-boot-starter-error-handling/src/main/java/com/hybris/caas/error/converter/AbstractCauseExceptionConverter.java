package com.hybris.caas.error.converter;

import com.hybris.caas.error.ErrorMessage;

/**
 * Convert an exception to a {@link ErrorMessage} by retrying the exception
 * conversion process with the current exception's cause or root cause.
 */
public abstract class AbstractCauseExceptionConverter<T extends Throwable> extends AbstractExceptionConverter<T>
{
	@Override
	protected ErrorMessage convert(final T ex)
	{
		throw new UnsupportedOperationException("Root cause based exception converters should be converted directly in the exception converter factory.");
	}

	/**
	 * Whether to use the root cause or the immediate cause.
	 *
	 * @return {@code true} if the root cause should be used; {@code false} if the immediate cause should be used
	 */
	protected abstract boolean useRootCause();
}
