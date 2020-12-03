package com.hybris.caas.multitenant.service.exception;

/**
 * This exception should be thrown when the tenant provided in the request via x-forwarded-host contains illegal characters.
 */
@SuppressWarnings("serial")
public class InvalidTenantFormatException extends RuntimeException
{
	public static final String MESSAGE = "Tenant must conform to domain specifications and may only contain the following character sets: a-z 0-9 - or .";

	public InvalidTenantFormatException()
	{
		super(MESSAGE);
	}
}
