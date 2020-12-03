package com.hybris.caas.multitenant.service.exception;

/**
 * This exception should be thrown when the tenant information is missing, when it was required.
 */
public class MissingTenantException extends RuntimeException
{
	private static final long serialVersionUID = 1273730130052391258L;
	public static final String MESSAGE = "Unable to extract tenant information from HTTP request.";

	public enum AccessType
	{
		PUBLIC, PROTECTED
	}

	private final AccessType accessType;

	public MissingTenantException(final AccessType accessType)
	{
		super(MESSAGE);
		this.accessType = accessType;
	}

	public MissingTenantException(final AccessType accessType, Throwable cause)
	{
		super(MESSAGE, cause);
		this.accessType = accessType;
	}

	public AccessType getAccessType()
	{
		return accessType;
	}
}
