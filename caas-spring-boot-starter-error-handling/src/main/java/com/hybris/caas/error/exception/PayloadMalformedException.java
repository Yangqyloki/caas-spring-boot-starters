package com.hybris.caas.error.exception;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.annotation.WebException;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;

/**
 * Exception to be thrown when the request body cannot be parsed.
 */
@WebException(status = HttpStatus.BAD_REQUEST, type = ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX)
public class PayloadMalformedException extends RuntimeException
{
	private static final long serialVersionUID = 4275470810861069150L;

	public PayloadMalformedException()
	{
		super(String.format(EXCEPTION_MESSAGE_BODY_INVALID));
	}

	public PayloadMalformedException(final Throwable cause)
	{
		super(String.format(EXCEPTION_MESSAGE_BODY_INVALID), cause);
	}
}
