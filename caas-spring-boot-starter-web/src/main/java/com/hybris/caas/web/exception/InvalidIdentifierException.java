package com.hybris.caas.web.exception;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.annotation.WebException;
import org.springframework.http.HttpStatus;

/**
 * Exception to be thrown when a resource identifier used in a path segment failed validation.
 * Even thought this is related to a validation failure, we want to map this exception to a 404 - NOT FOUND since
 * if the validation failed, then we can guarantee that the resource does not exist.
 */
@WebException(status = HttpStatus.NOT_FOUND, type = ErrorConstants.TYPE_404_ELEMENT_RESOURCE_NOT_EXISTING)
public class InvalidIdentifierException extends RuntimeException
{
	private static final long serialVersionUID = 7734451050257924646L;
	private static final String MESSAGE = "Resource not found with identifier: %s";

	public InvalidIdentifierException(final String identifier)
	{
		super(String.format(MESSAGE, identifier));
	}

}
