package com.hybris.caas.web.validator.utils;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Set;

/**
 * Defines validation related methods.
 */
public final class ValidatorUtils
{
	private static final String VIOLATION_EXCEPTION = "Validation exception";
	private ValidatorUtils()
	{
		// empty
	}
	/**
	 * Validate constraint violation for a given dto.
	 *
	 * @param dto       the dto to validate.
	 * @param validator the validator to apply.
	 */
	public static <T> void validateDto(final T dto, final Validator validator)
	{
		final Set<ConstraintViolation<T>> constraintViolations = validator.validate(dto);
		if (!constraintViolations.isEmpty())
		{
			throw new ConstraintViolationException(VIOLATION_EXCEPTION, constraintViolations);
		}
	}
}
