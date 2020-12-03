package com.hybris.caas.web.validator;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintValidatorContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.hybris.caas.web.Constants;
import com.hybris.caas.web.exception.InvalidIdentifierException;

@RunWith(MockitoJUnitRunner.class)
public class IdentifierValidatorTest
{
	private static final String VALID_UUID = "d9cffde6-7df6-11e7-bb31-be2e44b06b34";
	private static final String INVALID_UUID = "supercalifragilisticexpialidocious"; // 34 chars

	@Mock
	private Identifier regex;
	@Mock
	private ConstraintValidatorContext context;
	private IdentifierValidator validator;

	@Before
	public void setUp()
	{
		validator = new IdentifierValidator();

		when(regex.regex()).thenReturn(Constants.IDENTIFIER_REG_EXP);
		when(regex.min()).thenReturn(1);
		when(regex.max()).thenReturn(255);
	}

	@Test
	public void should_validate_regex()
	{
		validator.initialize(regex);
		final boolean isValid = validator.isValid(VALID_UUID, context);
		assertTrue(isValid);
	}

	@Test(expected = InvalidIdentifierException.class)
	public void should_throw_exception_when_fail_validate_regex()
	{
		validator.initialize(regex);
		validator.isValid(INVALID_UUID, context);
	}

	@Test(expected = InvalidIdentifierException.class)
	public void should_throw_exception_when_null_input()
	{
		validator.initialize(regex);
		validator.isValid(null, context);
	}

	@Test(expected = InvalidIdentifierException.class)
	public void should_throw_exception_when_input_less_than_min()
	{
		validator.initialize(regex);
		validator.isValid("", context);
	}

	@Test(expected = InvalidIdentifierException.class)
	public void should_throw_exception_when_input_more_than_max()
	{
		when(regex.max()).thenReturn(33);

		validator.initialize(regex);
		validator.isValid(INVALID_UUID, context);
	}
}
