package com.hybris.caas.web.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validate the given String field against the {@link Enum} values.
 * Valid if the string is null or if the string matches the enum values.
 * To check the value is not null, use {@link NotNull}
 */
public class EnumValuesValidator implements ConstraintValidator<EnumValues, String>
{

	private final Set<String> validValues = new LinkedHashSet<>();

	@SuppressWarnings("rawtypes")
	@Override
	public void initialize(final EnumValues constraintAnnotation)
	{
		final Class<? extends Enum> enumClass = constraintAnnotation.value();
		final Enum[] enumConstants = enumClass.getEnumConstants();
		Arrays.stream(enumConstants).map(Enum::name).collect(Collectors.toCollection(() -> validValues));
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context)
	{
		final boolean isValid = value == null || validValues.contains(value);

		if (!isValid)
		{
			final String messageTemplate = context.getDefaultConstraintMessageTemplate();
			final String message = String.format(messageTemplate, validValues.toString());
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
		}

		return isValid;
	}

}
