package com.hybris.caas.web.validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocaleValidatorTest
{
	@Mock
	private ConstraintValidatorContext context;

	@Captor
	private ArgumentCaptor<String> captor;

	private LocaleValidator validator = new LocaleValidator();

	@Before
	public void setUp() throws Exception
	{
		when(context.getDefaultConstraintMessageTemplate()).thenReturn("dummy-message %s");
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(
				mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));
	}

	@Test
	public void should_mark_valid_a_null_locale_string()
	{
		assertTrue(validator.isValid(null, context));

		verify(context, never()).buildConstraintViolationWithTemplate(anyString());
	}

	private void assertConstraintViolation(final String invalidLocale)
	{
		assertFalse(validator.isValid(invalidLocale, context));

		verify(context, times(1)).buildConstraintViolationWithTemplate(captor.capture());
		assertThat(captor.getValue()).endsWith(invalidLocale);
	}

	@Test
	public void should_mark_invalid_a_locale_string_with_invalid_format()
	{
		assertFalse(validator.isValid("en", context));
		assertFalse(validator.isValid("und", context));
		assertFalse(validator.isValid("sr-Lat", context));
		assertFalse(validator.isValid("sr-Latn", context));
		assertFalse(validator.isValid("sr-Latin", context));
		assertFalse(validator.isValid("sr-Latn", context));
		assertFalse(validator.isValid("en-BTG", context));
		assertFalse(validator.isValid("en_US", context));
		assertFalse(validator.isValid("eN-US", context));
		assertFalse(validator.isValid("en-uS", context));
		assertFalse(validator.isValid("en-Us", context));
		assertFalse(validator.isValid("sr-Latin-RS", context));
		assertFalse(validator.isValid("sr-LaTn-RS", context));
		assertFalse(validator.isValid("sr-latn-RS", context));
		assertFalse(validator.isValid("sr-LAtn-RS", context));
		assertFalse(validator.isValid("sr-LatN-RS", context));
		assertFalse(validator.isValid("sr-Lat-RS", context));
		assertFalse(validator.isValid("sr-L-RS", context));
		assertFalse(validator.isValid("sr--RS", context));
	}

	@Test
	public void should_mark_invalid_a_non_available_or_not_allowed_locale_string()
	{
		assertConstraintViolation("en-BT");
	}

	@Test
	public void should_mark_invalid_a_not_allowed_locale_string()
	{
		assertConstraintViolation("en-Latn-US");
	}

	@Test
	public void should_mark_valid_an_known_locale_string()
	{
		assertTrue(validator.isValid("en-US", context));

		verify(context, never()).buildConstraintViolationWithTemplate(anyString());
	}

	@Test
	public void should_mark_valid_an_known_locale_string_with_script()
	{
		assertTrue(validator.isValid("sr-Latn-RS", context));

		verify(context, never()).buildConstraintViolationWithTemplate(anyString());
	}
}
