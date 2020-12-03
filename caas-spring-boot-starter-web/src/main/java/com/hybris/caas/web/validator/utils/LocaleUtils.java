package com.hybris.caas.web.validator.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hybris.caas.web.Constants.LOCALE_REG_EXP;
import static com.hybris.caas.web.Constants.LOCALE_WITH_SCRIPT_REG_EXP;
import static org.apache.commons.lang3.LocaleUtils.isAvailableLocale;

/**
 * Provides utility methods for handling locale conversion and validation.
 */
public class LocaleUtils
{
	private static final String DASH = "-";
	private static final String UNDERSCORE = "_";

	private static final Pattern LANGUAGE_PATTERN = Pattern.compile("^[a-z]{2}$");
	private static final Pattern COUNTRY_PATTERN = Pattern.compile("^[A-Z]{2}$");
	private static final Pattern SCRIPT_PATTERN = Pattern.compile("^[A-Z]{1}[a-z]{3}$");
	private static final Pattern LOCALE_WITH_LANGUAGE_COUNTRY_PATTERN = Pattern.compile(LOCALE_REG_EXP);
	private static final Pattern LOCALE_WITH_LANGUAGE_SCRIPT_COUNTRY_PATTERN = Pattern.compile(LOCALE_WITH_SCRIPT_REG_EXP);

	private static final Set<String> AVAILABLE_LOCALE_TAGS = Arrays.stream(Locale.getAvailableLocales())
			.map(Locale::toLanguageTag)
			.collect(Collectors.toSet());
	private static final Set<Locale> ALLOWED_SCRIPT_BASED_LOCALES = Arrays.stream(Locale.getAvailableLocales())
			.filter(locale -> StringUtils.isNotEmpty(locale.getLanguage()) && StringUtils.isNotEmpty(locale.getCountry())
					&& StringUtils.isNotEmpty(locale.getScript()))
			.collect(Collectors.toSet());
	private static final Set<String> ALLOWED_SCRIPT_BASED_LOCALE_TAGS = ALLOWED_SCRIPT_BASED_LOCALES.stream()
			.map(Locale::toLanguageTag)
			.collect(Collectors.toSet());

	private LocaleUtils()
	{
		// empty
	}

	/**
	 * Checks that {@link Locale} parameter is a valid locale.
	 * - validates that language of the {@link Locale} is 2 chars in lowercase
	 * - validates that country of the {@link Locale} is 2 chars in uppercase
	 * - validates that optional script of the {@link Locale} is 4 chars with first char in uppercase and rest in lowercase
	 * - validates that the {@link Locale} represents an available Java {@link Locale}
	 *
	 * @param locale the locale to be validated
	 * @return true if the {@link Locale} is valid, false otherwise (null is considered invalid locale)
	 */
	public static boolean isLocaleValid(final Locale locale)
	{
		if (Objects.isNull(locale))
		{
			return false;
		}

		if (!LANGUAGE_PATTERN.matcher(locale.getLanguage()).matches() || !COUNTRY_PATTERN.matcher(locale.getCountry()).matches() || (
				StringUtils.isNotEmpty(locale.getScript()) && !SCRIPT_PATTERN.matcher(locale.getScript()).matches()))
		{
			return false;
		}

		return StringUtils.isNotEmpty(locale.getScript()) ? ALLOWED_SCRIPT_BASED_LOCALES.contains(locale) : isAvailableLocale(locale);
	}

	/**
	 * Checks that {@link String} parameter is a valid locale.
	 * - validates that language of the locale is 2 chars in lowercase
	 * - validates that country of the locale is 2 chars in uppercase
	 * - validates that optional script of the locale is 4 chars with first char in uppercase and rest in lowercase
	 * - validates that the language, script, and country are separated by a dash
	 * - validates that the locale represents an available Java {@link Locale}
	 *
	 * @param locale the locale to be validated
	 * @return true if the {@link Locale} is valid, false otherwise (null is considered invalid locale)
	 */
	public static boolean isLocaleValid(final String locale)
	{
		if (Objects.isNull(locale))
		{
			return false;
		}

		if (!LOCALE_WITH_LANGUAGE_COUNTRY_PATTERN.matcher(locale).matches())
		{
			return LOCALE_WITH_LANGUAGE_SCRIPT_COUNTRY_PATTERN.matcher(locale).matches() && ALLOWED_SCRIPT_BASED_LOCALE_TAGS.contains(
					locale);
		}

		return AVAILABLE_LOCALE_TAGS.contains(locale);
	}

	/**
	 * Converts {@link String} parameter to {@link Locale}.
	 * - validates that parameter provides 2 chars in lowercase for language
	 * - validates that parameter provides 2 chars in uppercase for country
	 * - validates that parameter provides for optional script 4 chars with first char in uppercase and rest in lowercase
	 * - validates that the language, script, and country are separated by a dash
	 * <p>
	 * Important: it does not enforce that the locale string represents an available and allowed Java {@link Locale}. This method
	 * is intended to be used used to convert an already validated locale string into a {@link Locale} object.
	 *
	 * @param locale the {@link String} representing the locale
	 * @return the {@link Locale} built from the {@link String} parameter or null if null was provided
	 * @throws IllegalArgumentException if provided parameter does not have the expected format: ll-CC or ll-Ssss-CC
	 */
	public static Locale toLocale(final String locale)
	{
		if (Objects.isNull(locale))
		{
			return null;
		}

		if (!(LOCALE_WITH_LANGUAGE_COUNTRY_PATTERN.matcher(locale).matches() || LOCALE_WITH_LANGUAGE_SCRIPT_COUNTRY_PATTERN.matcher(
				locale).matches()))
		{
			throw new IllegalArgumentException(
					String.format("Invalid locale format. Required format: %s or %s", LOCALE_REG_EXP, LOCALE_WITH_SCRIPT_REG_EXP));
		}

		return LOCALE_WITH_LANGUAGE_COUNTRY_PATTERN.matcher(locale).matches() ?
				org.apache.commons.lang3.LocaleUtils.toLocale(replaceLanguageCountrySeparator(locale)) :
				Locale.forLanguageTag(locale);
	}

	private static String replaceLanguageCountrySeparator(final String locale)
	{
		if (Objects.isNull(locale))
		{
			return null;
		}

		return locale.replace(DASH, UNDERSCORE);
	}
}
