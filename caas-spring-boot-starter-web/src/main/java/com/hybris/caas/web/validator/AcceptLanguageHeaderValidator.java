package com.hybris.caas.web.validator;

import com.hybris.caas.error.exception.InvalidHttpRequestHeaderException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Objects;

import static com.hybris.caas.web.Constants.STAR_WILDCARD;
import static com.hybris.caas.web.validator.utils.LocaleUtils.isLocaleValid;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;

/**
 * Validate the given parameter against the supported locale format and that the locale represents an available Java {@link Locale}.
 * -locale format:  2 chars in lowercase for language, optional script with 4 chars with first char in uppercase and rest in lowercase,
 * and 2 chars in uppercase for country separated by dash
 */
public class AcceptLanguageHeaderValidator implements ConstraintValidator<AcceptLanguageHeader, String>
{
	private boolean lenient;
	private boolean allowWildcard;

	@Override
	public void initialize(final AcceptLanguageHeader constraintAnnotation)
	{
		lenient = constraintAnnotation.lenient();
		allowWildcard = constraintAnnotation.allowWildcard();
	}

	/**
	 * Validates the value of the `Accept-Language` header according to {@link AcceptLanguageHeader#allowWildcard()} and {@link AcceptLanguageHeader#lenient()} configuration flags.
	 * <p>
	 * "*" is only allowed if {@link AcceptLanguageHeader#allowWildcard()} is set to true.
	 * Locale validation is only performed if {@link AcceptLanguageHeader#lenient()} is set to false.
	 * <p>
	 * The given parameter is validated against the supported locale format and that the locale represents an available Java {@link Locale}.
	 * -locale format:  2 chars in lowercase for language, optional script with 4 chars with first char in uppercase and rest in lowercase,
	 * and 2 chars in uppercase for country separated by dash
	 *
	 * @param value   the parameter that needs to be validated
	 * @param context the validation context
	 * @return true if the parameter is valid, otherwise false (null is valid)
	 * @throws InvalidHttpRequestHeaderException if the parameter does no represent a valid locale or not allowed as per configuration flags
	 */
	@Override
	@SuppressWarnings("squid:S3516")
	public boolean isValid(final String value, final ConstraintValidatorContext context)
	{
		if (Objects.isNull(value))
		{
			return true;
		}

		if (STAR_WILDCARD.equals(value.trim()))
		{
			if (allowWildcard)
			{
				return true;
			}
			else
			{
				throw new InvalidHttpRequestHeaderException(ACCEPT_LANGUAGE);
			}
		}

		if (!lenient && !isLocaleValid(value))
		{
			throw new InvalidHttpRequestHeaderException(ACCEPT_LANGUAGE);
		}

		return true;
	}
}
