package com.hybris.caas.web.validator;

import com.hybris.caas.error.exception.InvalidHttpRequestHeaderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AcceptLanguageHeaderValidatorTest
{
	@Mock
	private AcceptLanguageHeader acceptLanguageHeader;
	@Mock
	private ConstraintValidatorContext constraintValidatorContext;

	private final AcceptLanguageHeaderValidator validator = new AcceptLanguageHeaderValidator();

	@Test
	public void should_be_true_when_valid_locale()
	{
		when(acceptLanguageHeader.lenient()).thenReturn(false);
		when(acceptLanguageHeader.allowWildcard()).thenReturn(true);

		validator.initialize(acceptLanguageHeader);
		assertTrue(validator.isValid("en-US", constraintValidatorContext));
	}

	@Test
	public void should_be_true_when_wildcard()
	{
		when(acceptLanguageHeader.lenient()).thenReturn(false);
		when(acceptLanguageHeader.allowWildcard()).thenReturn(true);

		validator.initialize(acceptLanguageHeader);
		assertTrue(validator.isValid("*", constraintValidatorContext));
	}


	@Test
	public void should_be_true_when_lenient_and_invalid()
	{
		when(acceptLanguageHeader.lenient()).thenReturn(true);
		when(acceptLanguageHeader.allowWildcard()).thenReturn(true);

		validator.initialize(acceptLanguageHeader);
		assertTrue(validator.isValid("invalid", constraintValidatorContext));
	}

	@Test
	public void should_throw_exception_when_invalid_locale_and_not_lenient_config()
	{
		when(acceptLanguageHeader.lenient()).thenReturn(false);
		when(acceptLanguageHeader.allowWildcard()).thenReturn(true);

		validator.initialize(acceptLanguageHeader);
		assertThrows(InvalidHttpRequestHeaderException.class, () -> {
			validator.isValid("invalid", constraintValidatorContext);
		});
	}

	@Test
	public void should_throw_exception_when_wildcard_and_not_allowed_wildcard_config()
	{
		when(acceptLanguageHeader.lenient()).thenReturn(false);
		when(acceptLanguageHeader.allowWildcard()).thenReturn(false);

		validator.initialize(acceptLanguageHeader);
		assertThrows(InvalidHttpRequestHeaderException.class, () -> {
			validator.isValid("*", constraintValidatorContext);
		});
	}

}
