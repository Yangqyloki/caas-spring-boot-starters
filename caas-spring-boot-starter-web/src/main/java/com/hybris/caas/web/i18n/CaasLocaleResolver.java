package com.hybris.caas.web.i18n;

import com.hybris.caas.error.exception.InvalidHttpRequestHeaderException;
import com.hybris.caas.web.config.I18nProperties;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Objects;

import static com.hybris.caas.web.Constants.ALL_LANGUAGES_LOCALE;
import static com.hybris.caas.web.Constants.STAR_WILDCARD;
import static com.hybris.caas.web.validator.utils.LocaleUtils.isLocaleValid;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;

/**
 * Custom implementation of {@link LocaleResolver} that will behave as following:
 * - when Accept-Language header is not provided the returned locale is going to be Locale.US
 * - when Accept-Language header is set to "*":
 * a) allow it or not according to the {@link I18nProperties} allowWildcard flag configuration
 * b) the returned locale is a constant to be used as all languages indicator {@link com.hybris.caas.web.Constants#ALL_LANGUAGES_LOCALE}
 * - validate that language of the {@link Locale} retrieved from {@link HttpServletRequest} is 2 chars in lowercase
 * - validate that country of the {@link Locale} retrieved from {@link HttpServletRequest} is 2 chars in uppercase
 * - validate that optional script of the {@link Locale} retrieved from {@link HttpServletRequest} is 4 chars with first char in uppercase and rest in lowercase
 * - validate that the {@link Locale} retrieved from {@link HttpServletRequest} represents an available Java {@link Locale}
 * - the validation mentioned above is applied or not depending on the {@link I18nProperties} lenient flag configuration
 * <p>
 * If validation fails {@link InvalidHttpRequestHeaderException} is thrown.
 */
public class CaasLocaleResolver implements LocaleResolver
{
	private final boolean allowWildcard;
	private final boolean lenient;

	public CaasLocaleResolver(final I18nProperties i18nProperties)
	{
		this.allowWildcard = i18nProperties.getLocaleResolver().isAllowWildcard();
		this.lenient = i18nProperties.getLocaleResolver().isLenient();
	}

	@Override
	public Locale resolveLocale(final HttpServletRequest request)
	{
		final String requestHeader = request.getHeader(ACCEPT_LANGUAGE);

		if (Objects.isNull(requestHeader))
		{
			return Locale.US;
		}

		if (STAR_WILDCARD.equals(requestHeader.trim()))
		{
			if (allowWildcard)
			{
				return ALL_LANGUAGES_LOCALE;
			}
			else
			{
				throw new InvalidHttpRequestHeaderException(ACCEPT_LANGUAGE);
			}
		}

		final Locale requestLocale = request.getLocale();
		if (!lenient && !isLocaleValid(requestLocale))
		{
			throw new InvalidHttpRequestHeaderException(ACCEPT_LANGUAGE);
		}

		return requestLocale;
	}

	@Override
	public void setLocale(final HttpServletRequest request, final HttpServletResponse response, final Locale locale)
	{
		throw new UnsupportedOperationException("Cannot change HTTP accept header - use a different locale resolution strategy.");
	}
}
