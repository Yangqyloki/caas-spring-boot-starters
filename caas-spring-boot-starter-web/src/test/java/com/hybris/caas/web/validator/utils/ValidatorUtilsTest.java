package com.hybris.caas.web.validator.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Collections;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidatorUtilsTest
{
	@Mock
	private Validator validator;
	@Mock
	private ConstraintViolation<Object> constraintViolation;

	@Test(expected = ConstraintViolationException.class)
	public void should_throw_ConstraintViolationException_when_invalid_object_is_passed()
	{
		// Given
		final Object invalidObject = new Object();
		when(validator.validate(invalidObject)).thenReturn(singleton(constraintViolation));

		// When
		ValidatorUtils.validateDto(invalidObject, validator);
	}

	@Test
	public void should_return_ok()
	{
		// Given
		final Object okObject = new Object();
		when(validator.validate(okObject)).thenReturn(Collections.emptySet());

		// When
		ValidatorUtils.validateDto(okObject, validator);
	}
}
