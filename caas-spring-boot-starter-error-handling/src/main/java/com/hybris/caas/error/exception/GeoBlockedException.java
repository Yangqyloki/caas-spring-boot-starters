package com.hybris.caas.error.exception;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.annotation.WebException;
import org.springframework.http.HttpStatus;

/**
 * Exception to be thrown when attempting to access a resource that resides in a restricted geo location.
 */
@WebException(status = HttpStatus.FORBIDDEN, type = ErrorConstants.TYPE_403_IP_BLOCKED)
public class GeoBlockedException extends RuntimeException
{
	private static final long serialVersionUID = -1633990435652736350L;
	private static final String MESSAGE = "Access to host is forbidden.";

	public GeoBlockedException()
	{
		super(MESSAGE);
	}

}
