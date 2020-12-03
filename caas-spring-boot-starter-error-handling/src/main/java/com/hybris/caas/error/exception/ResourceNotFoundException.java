package com.hybris.caas.error.exception;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.annotation.WebException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

/**
 * Exception to be thrown when trying to retrieve a non-existing resource.
 */
@WebException(status = HttpStatus.NOT_FOUND, type = ErrorConstants.TYPE_404_ELEMENT_RESOURCE_NOT_EXISTING)
public class ResourceNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "Resource not found with identifier(s): %s";
	private static final String GENERIC_MESSAGE = "Resource not found.";

	public ResourceNotFoundException(final String... ids)
	{
		super(String.format(MESSAGE, Arrays.asList(ids)));
	}

	public ResourceNotFoundException()
	{
		super(GENERIC_MESSAGE);
	}
}
