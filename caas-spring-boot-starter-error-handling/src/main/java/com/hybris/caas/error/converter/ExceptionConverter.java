package com.hybris.caas.error.converter;

import com.hybris.caas.error.ErrorMessage;

/**
 * Interface to convert a {@link Throwable} to a {@link ErrorMessage}.
 */
public interface ExceptionConverter
{

	/**
	 * Convert the exception to a canonical error message.
	 *
	 * @param ex
	 *            - the exception to convert
	 * @return the error message
	 */
	ErrorMessage toErrorMessage(Throwable ex);

}
