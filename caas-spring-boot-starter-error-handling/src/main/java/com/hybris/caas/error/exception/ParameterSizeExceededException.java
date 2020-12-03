package com.hybris.caas.error.exception;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.annotation.WebException;
import org.springframework.http.HttpStatus;

/**
 * Exception to be thrown when the URI parameter length of the request exceeds a configured limit.
 */
@WebException(status = HttpStatus.BAD_REQUEST, type = ErrorConstants.SUB_TYPE_400_INVALID_QUERY_PARAMETER)
public class ParameterSizeExceededException extends RuntimeException
{
	private static final long serialVersionUID = 1490291807662951556L;
	private static final String MESSAGE = "The query parameter '%s' exceeds the maximum size allowed. Maximum allowed '%s', given '%s'";

	public ParameterSizeExceededException(final String paramName, final int maxParamLength, final int paramLenght)
	{
		super(String.format(MESSAGE, paramName, maxParamLength, paramLenght));
	}

}
