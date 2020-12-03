package com.hybris.caas.web.validator;

import com.hybris.caas.web.validator.utils.LocaleUtils;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LocaleUtilsTest
{
	@Test
	public void should_mark_invalid_null_locale()
	{
		assertFalse(LocaleUtils.isLocaleValid((Locale) null));
	}

	@Test
	public void should_mark_invalid_locale_with_language_only()
	{
		assertFalse(LocaleUtils.isLocaleValid(new Locale("en")));
	}

	@Test
	public void should_mark_invalid_locale_language_and_script()
	{
		assertFalse(LocaleUtils.isLocaleValid(Locale.forLanguageTag("sr-Latn")));
	}

	@Test
	public void should_mark_invalid_locale_with_invalid_language()
	{
		assertFalse(LocaleUtils.isLocaleValid(new Locale("und")));
	}

	@Test
	public void should_mark_invalid_non_available_locale()
	{
		assertFalse(LocaleUtils.isLocaleValid(new Locale("en", "BT")));
	}

	@Test
	public void should_mark_invalid_locale_with_unsupported_script()
	{
		assertFalse(LocaleUtils.isLocaleValid(Locale.forLanguageTag("sr-Hans-RS")));
	}

	@Test
	public void should_mark_invalid_locale_with_invalid_script()
	{
		assertFalse(LocaleUtils.isLocaleValid(Locale.forLanguageTag("sr-Latin-RS")));
	}

	@Test
	public void should_mark_invalid_locale_with_invalid_country()
	{
		assertFalse(LocaleUtils.isLocaleValid(new Locale("en", "BTG")));
	}

	@Test
	public void should_mark_valid_known_locale()
	{
		assertTrue(LocaleUtils.isLocaleValid(new Locale("en", "US")));
		assertTrue(LocaleUtils.isLocaleValid(new Locale("eN", "Us")));
	}

	@Test
	public void should_mark_invalid_locale_that_does_not_require_script()
	{
		assertFalse(LocaleUtils.isLocaleValid(Locale.forLanguageTag("en-Latn-US")));
	}

	@Test
	public void should_mark_valid_known_locale_with_script()
	{
		assertTrue(LocaleUtils.isLocaleValid(Locale.forLanguageTag("sr-Latn-RS")));
		assertTrue(LocaleUtils.isLocaleValid(Locale.forLanguageTag("sr-LaTn-RS")));
		assertTrue(LocaleUtils.isLocaleValid(Locale.forLanguageTag("sr-latn-RS")));
		assertTrue(LocaleUtils.isLocaleValid(Locale.forLanguageTag("sr-LAtn-RS")));
		assertTrue(LocaleUtils.isLocaleValid(Locale.forLanguageTag("sr-LatN-RS")));
	}

	@Test
	public void should_mark_invalid_null_locale_string()
	{
		assertFalse(LocaleUtils.isLocaleValid((String) null));
	}

	@Test
	public void should_mark_invalid_locale_string_with_language_only()
	{
		assertFalse(LocaleUtils.isLocaleValid("en"));
	}

	@Test
	public void should_mark_invalid_locale_string_with_language_and_script()
	{
		assertFalse(LocaleUtils.isLocaleValid("sr-Latn"));
	}

	@Test
	public void should_mark_invalid_locale_string_with_invalid_format()
	{
		assertFalse(LocaleUtils.isLocaleValid("un-UND"));
		assertFalse(LocaleUtils.isLocaleValid("en_US"));
		assertFalse(LocaleUtils.isLocaleValid("eN-US"));
		assertFalse(LocaleUtils.isLocaleValid("en-uS"));
		assertFalse(LocaleUtils.isLocaleValid("en-Us"));
	}

	@Test
	public void should_mark_invalid_non_available_locale_string()
	{
		assertFalse(LocaleUtils.isLocaleValid("en-BT"));
	}

	@Test
	public void should_mark_invalid_locale_string_with_unsupported_script()
	{
		assertFalse(LocaleUtils.isLocaleValid("sr-Hans-RS"));
	}

	@Test
	public void should_mark_invalid_locale_string_with_invalid_script()
	{
		assertFalse(LocaleUtils.isLocaleValid("sr-Latin-RS"));
		assertFalse(LocaleUtils.isLocaleValid("sr-LaTn-RS"));
		assertFalse(LocaleUtils.isLocaleValid("sr-latn-RS"));
		assertFalse(LocaleUtils.isLocaleValid("sr-LAtn-RS"));
		assertFalse(LocaleUtils.isLocaleValid("sr-LatN-RS"));
		assertFalse(LocaleUtils.isLocaleValid("sr-Lat-RS"));
		assertFalse(LocaleUtils.isLocaleValid("sr-L-RS"));
		assertFalse(LocaleUtils.isLocaleValid("sr--RS"));
	}

	@Test
	public void should_mark_valid_known_locale_string()
	{
		assertTrue(LocaleUtils.isLocaleValid("en-US"));
	}

	@Test
	public void should_mark_invalid_locale_string_that_does_not_require_script()
	{
		assertFalse(LocaleUtils.isLocaleValid("en-Latn-US"));
	}

	@Test
	public void should_mark_valid_known_locale_string_with_script()
	{
		assertTrue(LocaleUtils.isLocaleValid(Locale.forLanguageTag("sr-Latn-RS")));
	}

	@Test
	public void should_return_null_when_null_is_provided_for_convertion_to_locale()
	{
		assertNull(LocaleUtils.toLocale(null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_IllegalArgumentException_when_converting_to_locale_a_string_with_language_only()
	{
		LocaleUtils.toLocale("en");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_IllegalArgumentException_when_converting_to_locale_a_string_with_language_and_script()
	{
		LocaleUtils.toLocale("sr-Latn");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_IllegalArgumentException_when_converting_to_locale_a_string_with_invalid_country_format()
	{
		LocaleUtils.toLocale("un-UND");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_IllegalArgumentException_when_converting_to_locale_a_string_with_invalid_script_format()
	{
		LocaleUtils.toLocale("sr-Lat");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_IllegalArgumentException_when_converting_to_locale_a_string_with_invalid_script_case_format()
	{
		LocaleUtils.toLocale("sr-LaTn-RS");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_IllegalArgumentException_when_converting_to_locale_a_string_with_invalid_country_case_format()
	{
		LocaleUtils.toLocale("sr-Latn-Rs");
	}

	@Test
	public void should_not_throw_IllegalArgumentException_when_converting_to_locale_a_non_available_or_non_allowed_locale_string()
	{
		assertNotNull(LocaleUtils.toLocale("en-BT"));
		assertNotNull(LocaleUtils.toLocale("en-Latn-BT"));
	}

	@Test
	public void should_convert_to_locale_known_locale_string()
	{
		assertEquals(Locale.US, LocaleUtils.toLocale("en-US"));
	}

	@Test
	public void should_convert_to_locale_known_locale_string_with_script()
	{
		final Locale result = LocaleUtils.toLocale("sr-Latn-RS");
		assertEquals("sr", result.getLanguage());
		assertEquals("RS", result.getCountry());
		assertEquals("Latn", result.getScript());
	}
}
