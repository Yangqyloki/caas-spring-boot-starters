package com.hybris.caas.web.validator;

import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * Validates the object node max size.
 */
public class ObjectNodeMaxSizeValidator implements ConstraintValidator<ObjectNodeMaxSize, ObjectNode>
{
	private int max;

	@Override
	public void initialize(final ObjectNodeMaxSize constraintAnnotation)
	{
		max = constraintAnnotation.max();
	}

	@Override
	public boolean isValid(final ObjectNode value, final ConstraintValidatorContext context)
	{
		final boolean isValid = !(Objects.nonNull(value) && value.toString().length() > max);

		if (!isValid)
		{
			final String message = String.format(context.getDefaultConstraintMessageTemplate(), Integer.toString(max));
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
		}

		return isValid;
	}

}
