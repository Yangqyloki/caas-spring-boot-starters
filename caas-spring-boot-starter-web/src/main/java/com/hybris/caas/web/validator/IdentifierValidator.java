package com.hybris.caas.web.validator;

import java.util.Objects;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.hybris.caas.web.exception.InvalidIdentifierException;

/**
 * Validate the given parameter field against the regular expression provided in the annotation.
 * If the validation fails, a {@link InvalidIdentifierException} will be thrown.
 */
public class IdentifierValidator implements ConstraintValidator<Identifier, String>
{
	private Pattern pattern;
	private int min;
	private int max;

	@Override
	public void initialize(final Identifier constraintAnnotation)
	{
		pattern = Pattern.compile(constraintAnnotation.regex());
		min = constraintAnnotation.min();
		max = constraintAnnotation.max();
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context)
	{
		if (Objects.isNull(value) || min > value.length() || max < value.length() || !pattern.matcher(value).matches())
		{
			throw new InvalidIdentifierException(value);
		}
		return Boolean.TRUE;
	}

}
