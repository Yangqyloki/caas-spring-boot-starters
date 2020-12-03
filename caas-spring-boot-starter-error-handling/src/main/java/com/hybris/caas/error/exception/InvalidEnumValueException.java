package com.hybris.caas.error.exception;

import java.util.Set;

/**
 * Exception to be thrown when the value provided for an enum field is not valid and cannot be validated using the DTO.
 */
@SuppressWarnings("serial")
public class InvalidEnumValueException extends InvalidFieldException
{
	private static final String MESSAGE = "Invalid enum value. Valid types are: %s. Given: %s";

	public InvalidEnumValueException(final String fieldName, final Set<String> validValues, final String invalidValue)
	{
		super(String.format(MESSAGE, validValues.toString(), invalidValue), fieldName);
	}

	public InvalidEnumValueException(final String fieldName, final Set<String> validValues, final String invalidValue,
			final Throwable cause)
	{
		super(String.format(MESSAGE, validValues.toString(), invalidValue), fieldName, cause);
	}
}
