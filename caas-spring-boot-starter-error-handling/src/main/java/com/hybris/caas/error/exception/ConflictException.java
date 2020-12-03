package com.hybris.caas.error.exception;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.annotation.WebException;
import org.springframework.http.HttpStatus;

/**
 * Exception to be thrown when the operation would leave the resource in a conflicted state.
 */
@WebException(status = HttpStatus.CONFLICT, type = ErrorConstants.TYPE_409_CONFLICT_RESOURCE)
public class ConflictException extends RuntimeException
{
	public ConflictException(String message)
	{
		super(message);
	}

	public ConflictException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
