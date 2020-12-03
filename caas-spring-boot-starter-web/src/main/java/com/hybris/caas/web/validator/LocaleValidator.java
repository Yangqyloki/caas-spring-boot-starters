package com.hybris.caas.web.validator;

import com.hybris.caas.web.validator.utils.LocaleUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * Validate the given locale parameter against the supported locale format and that the locale represents an available Java {@link java.util.Locale}.
 * -locale format:  2 chars in lowercase for language code, optional script with 4 chars with first char in uppercase and rest in lowercase,
 * and 2 chars in uppercase for country code separated by dash
 */
public class LocaleValidator implements ConstraintValidator<Locale, String>
{
	@Override
	public void initialize(final Locale constraintAnnotation)
	{
		// Nothing to do here
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context)
	{
		if (Objects.isNull(value))
		{
			return true;
		}

		if (!LocaleUtils.isLocaleValid(value))
		{
			final String message = String.format(context.getDefaultConstraintMessageTemplate(), value);

			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(message).addConstraintViolation();

			return false;
		}

		return true;
	}
}
