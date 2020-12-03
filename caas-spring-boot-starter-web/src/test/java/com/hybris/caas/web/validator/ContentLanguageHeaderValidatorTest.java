package com.hybris.caas.web.validator;

import com.hybris.caas.error.exception.InvalidHttpRequestHeaderException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ContentLanguageHeaderValidatorTest
{
	private ContentLanguageHeaderValidator validator = new ContentLanguageHeaderValidator();

	@Test
	public void should_mark_valid_a_null_locale_string()
	{
		assertTrue(validator.isValid(null, null));
	}

	@Test(expected = InvalidHttpRequestHeaderException.class)
	public void should_throw_InvalidHttpRequestHeaderException_for_a_locale_string_with_language_only()
	{
		assertFalse(validator.isValid("en", null));
	}

	@Test(expected = InvalidHttpRequestHeaderException.class)
	public void should_throw_InvalidHttpRequestHeaderException_for_a_locale_string_with_invalid_language()
	{
		assertFalse(validator.isValid("und", null));
	}

	@Test(expected = InvalidHttpRequestHeaderException.class)
	public void should_throw_InvalidHttpRequestHeaderException_for_a_locale_string_with_invalid_script()
	{
		assertFalse(validator.isValid("sr-Lat", null));
	}

	@Test(expected = InvalidHttpRequestHeaderException.class)
	public void should_throw_InvalidHttpRequestHeaderException_for_a_locale_string_with_script_and_without_country()
	{
		assertFalse(validator.isValid("sr-Latin", null));
	}

	@Test(expected = InvalidHttpRequestHeaderException.class)
	public void should_throw_InvalidHttpRequestHeaderException_for_a_non_available_locale_string()
	{
		assertFalse(validator.isValid("en-BT", null));
	}

	@Test(expected = InvalidHttpRequestHeaderException.class)
	public void should_throw_InvalidHttpRequestHeaderException_for_a_non_allowed_locale_string()
	{
		assertFalse(validator.isValid("en-Latn-US", null));
	}

	@Test
	public void should_throw_InvalidHttpRequestHeaderException_for_a_locale_string_with_invalid_country()
	{
		try
		{
			validator.isValid("en-BTG", null);

			fail();
		}
		catch (InvalidHttpRequestHeaderException ex)
		{
			assertTrue(ex.getMessage().contains("Content-Language"));
		}
	}

	@Test(expected = InvalidHttpRequestHeaderException.class)
	public void should_throw_InvalidHttpRequestHeaderException_for_a_locale_string_with_script_and_an_invalid_country()
	{
		assertFalse(validator.isValid("sr-Latn-RSA", null));
	}

	@Test
	public void should_mark_valid_an_known_locale_string()
	{
		assertTrue(validator.isValid("en-US", null));
		assertTrue(validator.isValid("sr-Latn-RS", null));
	}

}
