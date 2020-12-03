package com.hybris.caas.error.exception;

/**
 * Exception to be thrown when the value provided for a given field is not valid.
 */
@SuppressWarnings("serial")
public class InvalidFieldException extends RuntimeException
{
	private final String fieldName;

	public InvalidFieldException(final String message, final String fieldName)
	{
		super(message);
		this.fieldName = fieldName;
	}

	public InvalidFieldException(final String message, final String fieldName, final Throwable cause)
	{
		super(message, cause);
		this.fieldName = fieldName;
	}

	public String getFieldName()
	{
		return fieldName;
	}
}
