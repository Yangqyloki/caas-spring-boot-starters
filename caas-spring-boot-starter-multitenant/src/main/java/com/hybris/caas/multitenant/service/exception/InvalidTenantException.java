package com.hybris.caas.multitenant.service.exception;

/**
 * This exception should be thrown when the tenant information between the JWT token and the header are inconsistent.
 */
@SuppressWarnings("serial")
public class InvalidTenantException extends RuntimeException
{
	public static final String MESSAGE = "Tenant information from access token is inconsistent with the tenant information found in the URL.";

	public InvalidTenantException()
	{
		super(MESSAGE);
	}

	public InvalidTenantException(Throwable cause)
	{
		super(MESSAGE, cause);
	}

}
