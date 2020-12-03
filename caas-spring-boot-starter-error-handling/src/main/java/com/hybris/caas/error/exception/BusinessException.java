package com.hybris.caas.error.exception;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.annotation.WebException;
import org.springframework.http.HttpStatus;

@WebException(status = HttpStatus.BAD_REQUEST, type = ErrorConstants.TYPE_400_BUSINESS_ERROR)
public class BusinessException extends RuntimeException
{
	private static final long serialVersionUID = 7764817600176885202L;

	public BusinessException(final String message)
	{
		super(message);
	}

	public BusinessException(final String message, final Throwable throwable)
	{
		super(message, throwable);
	}

	public static BusinessException of(final String message, final String... args)
	{
		return new BusinessException(String.format(message, (Object[]) args));
	}

	public static BusinessException of(final Throwable throwable, final String message, final String... args)
	{
		return new BusinessException(String.format(message, (Object[]) args), throwable);
	}

}
