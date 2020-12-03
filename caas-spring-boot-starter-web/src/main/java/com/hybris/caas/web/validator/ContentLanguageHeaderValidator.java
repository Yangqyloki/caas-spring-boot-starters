package com.hybris.caas.web.validator;

import com.hybris.caas.error.exception.InvalidHttpRequestHeaderException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Objects;

import static com.hybris.caas.web.validator.utils.LocaleUtils.isLocaleValid;
import static org.springframework.http.HttpHeaders.CONTENT_LANGUAGE;

/**
 * Validate the given parameter against the supported locale format and that the locale represents an available Java {@link Locale}.
 * -locale format:  2 chars in lowercase for language, optional script with 4 chars with first char in uppercase and rest in lowercase,
 * and 2 chars in uppercase for country separated by dash
 */
public class ContentLanguageHeaderValidator implements ConstraintValidator<ContentLanguageHeader, String>
{
	@Override
	public void initialize(final ContentLanguageHeader constraintAnnotation)
	{
		// Nothing to do here
	}

	/**
	 * Validate the given parameter against the supported locale format and that the locale represents an available Java {@link Locale}.
	 * -locale format:  2 chars in lowercase for language, optional script with 4 chars with first char in uppercase and rest in lowercase,
	 * and 2 chars in uppercase for country separated by dash
	 *
	 * @param value   the parameter that needs to be validated
	 * @param context the validation context
	 * @return true if the parameter is valid, otherwise false (null is valid)
	 * @throws InvalidHttpRequestHeaderException if the parameter does no represent a valid locale
	 */
	@Override
	@SuppressWarnings("squid:S3516")
	public boolean isValid(final String value, final ConstraintValidatorContext context)
	{
		if (Objects.isNull(value))
		{
			return true;
		}

		if (!isLocaleValid(value))
		{
			throw new InvalidHttpRequestHeaderException(CONTENT_LANGUAGE);
		}

		return true;
	}
}
