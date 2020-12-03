package com.hybris.caas.web.exception;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.annotation.WebException;
import org.springframework.http.HttpStatus;

/**
 * Exception to be thrown when the URI length of the request exceeds a configurable limit.
 */
@WebException(status = HttpStatus.BAD_REQUEST, type = ErrorConstants.TYPE_400_BUSINESS_ERROR)
public class UriLengthValidationException extends RuntimeException
{
	private static final long serialVersionUID = 8784361061965672622L;
	private static final String MESSAGE = "The URL length exceeds the maximum supported size. Max length %s, given %s.";

	public UriLengthValidationException(final int maxUriLength, final int uriLength)
	{
		super(String.format(MESSAGE, maxUriLength, uriLength));
	}
}
