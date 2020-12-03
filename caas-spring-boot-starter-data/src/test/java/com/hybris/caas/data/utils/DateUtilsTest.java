package com.hybris.caas.data.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.hybris.caas.data.utils.DateUtils.ISO8601_DATE_PATTERN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateUtilsTest
{
	private static final String DATE_STRING = "1971-02-25T02:03:00Z";
	private static final String INVALID_DATE_STRING = "1971-02-25T02:03:aaZ";
	private static final OffsetDateTime DATE = OffsetDateTime.of(1971, 2, 25, 2, 3, 0, 0, ZoneOffset.UTC);
	private static final String INVALID_DATE_PATTERN = "abc";
	private static final Long FEB_25_1971_02_03_00_UTC = 36295380000L;

	@Test
	public void should_return_null_when_parsing_null_date()
	{
		assertNull(DateUtils.parse(null, null));
	}

	@Test
	public void should_throw_exception_when_parsing_invalid_date()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			DateUtils.parse(INVALID_DATE_STRING, ISO8601_DATE_PATTERN);
		});
	}

	@Test
	public void should_throw_exception_when_parsing_with_invalid_pattern()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			DateUtils.parse(DATE_STRING, INVALID_DATE_PATTERN);
		});
	}

	@Test
	public void should_throw_exception_when_parsing_with_null_pattern()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			DateUtils.parse(DATE_STRING, null);
		});
	}

	@Test
	public void should_parse_date()
	{
		final OffsetDateTime result = DateUtils.parse(DATE_STRING, ISO8601_DATE_PATTERN);
		assertNotNull(result);
	}


	@Test
	public void should_parse_utc_date_using_implicit_iso8601_pattern()
	{
		final OffsetDateTime result = DateUtils.parseFromISO8601UTCString(DATE_STRING);
		assertThat(result.toInstant().toEpochMilli(), equalTo(FEB_25_1971_02_03_00_UTC));
	}

	@Test
	public void should_return_null_when_formatting_null_date()
	{
		assertNull(DateUtils.format(null, null));
	}

	@Test
	public void should_throw_exception_when_fomatting_date_using_null_pattern()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			DateUtils.format(DATE, null);
		});
	}

	@Test
	public void should_throw_exception_when_formatting_with_invalid_pattern()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			DateUtils.format(DATE, INVALID_DATE_PATTERN);
		});
	}

	@Test
	public void should_format_date()
	{
		final String result = DateUtils.format(DATE, ISO8601_DATE_PATTERN);
		assertNotNull(result);
	}


	@Test
	public void should_format_date_using_utc_iso8601_pattern()
	{
		final String result = DateUtils.formatToISO8601UTCString(OffsetDateTime.ofInstant(Instant.ofEpochMilli(FEB_25_1971_02_03_00_UTC), ZoneOffset.UTC));
		assertThat(result, equalTo(DATE_STRING));
	}

	@Test
	public void should_assert_null_as_valid_date()
	{
		assertTrue(DateUtils.isDate(null, null));
	}

	@Test
	public void should_assert_valid_date()
	{
		assertTrue(DateUtils.isDate(DATE_STRING, ISO8601_DATE_PATTERN));
	}

	@Test
	public void should_not_assert_invalid_date()
	{
		assertFalse(DateUtils.isDate(INVALID_DATE_STRING, ISO8601_DATE_PATTERN));
	}

	@Test
	public void should_throw_exception_when_asserting_valid_date_using_invalid_pattern()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			assertFalse(DateUtils.isDate(DATE_STRING, INVALID_DATE_PATTERN));
		});
	}

	@Test
	public void should_throw_exception_when_asserting_valid_date_using_null_pattern()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			assertFalse(DateUtils.isDate(DATE_STRING, null));
		});
	}
}
