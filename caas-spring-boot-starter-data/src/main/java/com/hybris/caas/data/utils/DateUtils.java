package com.hybris.caas.data.utils;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Provides utility methods for handling date conversion and exception translation.
 */
public final class DateUtils
{
	public static final String PATTERN_CANNOT_BE_NULL = "Pattern cannot be null.";
	public static final String ISO8601_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssX";

	private DateUtils()
	{
		// empty
	}

	public static OffsetDateTime parseFromISO8601UTCString(final String date)
	{
		return parse(date, ISO8601_DATE_PATTERN);
	}

	/**
	 * Parses a string representing a date using the provided pattern.
	 *
	 * @param date    the string representation of the date
	 * @param pattern the pattern to be used for parsing
	 * @return {@link OffsetDateTime} created from the string representation or null if the input string was null
	 * @throws IllegalArgumentException if pattern parameter is null or invalid
	 */
	public static OffsetDateTime parse(final String date, final String pattern)
	{
		if (Objects.isNull(date))
		{
			return null;
		}

		if (Objects.isNull(pattern))
		{
			throw new IllegalArgumentException(PATTERN_CANNOT_BE_NULL);
		}

		try
		{
			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern).withZone(ZoneOffset.UTC);
			return OffsetDateTime.parse(date, dtf);
		}
		catch (final DateTimeParseException e)
		{
			throw new IllegalArgumentException(String.format("Invalid date '%s' or pattern '%s'.", date, pattern), e);
		}
	}

	/**
	 * Parses a date format pattern, return a {@link DateTimeFormatter} instance.
	 *
	 * @param pattern the format pattern date to parse.
	 * @return an instance of {@code SimpleDateFormat} constructed from the date pattern.
	 * @throws NullPointerException     if pattern is {@code null}.
	 * @throws IllegalArgumentException if date format pattern is invalid.
	 */
	public static DateTimeFormatter parseDateFormat(final String pattern)
	{
		return DateTimeFormatter.ofPattern(requireNonNull(pattern, PATTERN_CANNOT_BE_NULL));
	}


	public static String formatToISO8601UTCString(final OffsetDateTime date)
	{
		return format(date, ISO8601_DATE_PATTERN);
	}

	/**
	 * Formats a date using the provided pattern.
	 *
	 * @param date    the date to be formatted
	 * @param pattern the pattern to be used for formatting the date
	 * @return string containing the formatted date or null if the date is null
	 * @throws IllegalArgumentException if pattern parameter is null or invalid
	 */
	public static String format(final OffsetDateTime date, final String pattern)
	{
		if (Objects.isNull(date))
		{
			return null;
		}

		if (Objects.isNull(pattern))
		{
			throw new IllegalArgumentException(PATTERN_CANNOT_BE_NULL);
		}

		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern).withZone(ZoneOffset.UTC);
		return dtf.format(date);
	}

	/**
	 * Checks that the provided string parameter is a date formatted as per provided input pattern.
	 *
	 * @param date    a string representation of the date
	 * @param pattern the pattern to be used for parsing the date
	 * @return true if provided string parameter is a date formatted as per provided input pattern, otherwise false.
	 * true will also be returned if the string representation of the date is null
	 * @throws IllegalArgumentException if pattern parameter is null or invalid
	 */
	public static boolean isDate(final String date, final String pattern)
	{
		if (Objects.isNull(date))
		{
			return true;
		}

		if (Objects.isNull(pattern))
		{
			throw new IllegalArgumentException(PATTERN_CANNOT_BE_NULL);
		}

		try
		{
			org.apache.commons.lang3.time.DateUtils.parseDateStrictly(date, pattern);
		}
		catch (final ParseException e)
		{
			return false;
		}

		return true;
	}

	public static OffsetDateTime offsetDateTimeNowUtc()
	{
		return OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
	}
}
