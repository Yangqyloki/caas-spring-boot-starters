package com.hybris.caas.web.exception;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.annotation.WebException;
import org.springframework.http.HttpStatus;

/**
 * Exception to be thrown when the amount of elements in an array exceeds a configured limit.
 */
@SuppressWarnings("serial")
@WebException(status = HttpStatus.BAD_REQUEST, type = ErrorConstants.TYPE_400_BUSINESS_ERROR)
public class TooManyElementsException extends RuntimeException
{
	private static final String MESSAGE = "Too many elements in the request. Maximum allowed is %s.";

	public TooManyElementsException(final int limit)
	{
		super(String.format(MESSAGE, limit));
	}
}
