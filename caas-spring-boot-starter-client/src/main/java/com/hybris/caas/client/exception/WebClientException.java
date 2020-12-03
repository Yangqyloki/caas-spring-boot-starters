package com.hybris.caas.client.exception;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.annotation.WebException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a web client REST call completes with an error. It encapsulates the cause of the error.
 */
@WebException(status = HttpStatus.INTERNAL_SERVER_ERROR, type = ErrorConstants.TYPE_500_BACKING_SERVICE_UNAVAILABLE)
public class WebClientException extends RuntimeException
{
	public WebClientException(final Throwable cause)
	{
		super(cause);
	}
}
