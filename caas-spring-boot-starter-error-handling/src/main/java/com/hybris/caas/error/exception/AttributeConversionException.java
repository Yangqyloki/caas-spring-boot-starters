package com.hybris.caas.error.exception;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.annotation.WebException;
import org.springframework.http.HttpStatus;

/**
 * Exception to be thrown when unable to convert an entity's attribute into the persistent storage.
 */
@WebException(status = HttpStatus.INTERNAL_SERVER_ERROR, type = ErrorConstants.TYPE_500_INTERNAL_SERVER_ERROR)
public class AttributeConversionException extends RuntimeException
{
	private static final long serialVersionUID = -6495641096881167455L;

	public AttributeConversionException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public AttributeConversionException(final String message)
	{
		super(message);
	}
}
