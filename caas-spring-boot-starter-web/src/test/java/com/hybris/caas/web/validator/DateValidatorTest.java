package com.hybris.caas.web.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static com.hybris.caas.data.utils.DateUtils.ISO8601_DATE_PATTERN;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DateValidatorTest
{
	private static final String PAST_DATE_STRING = "1971-02-25T02:03:00Z";
	private static final String FAR_FUTURE_DATE_STRING = "3000-02-25T02:03:00Z";
	private static final String INVALID_DATE_STRING = "1971-02-25T02:03:aaZ";
	private static final String INVALID_DATE_PATTERN = "abc";

	@Mock
	private Date date;
	@Mock
	private ConstraintValidatorContext context;
	@Mock
	private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

	private DateValidator validator;

	@BeforeEach
	public void setUp()
	{
		validator = new DateValidator();

	}

	@Test
	public void should_report_date_as_valid_when_correct_in_past()
	{
		when(date.pattern()).thenReturn(ISO8601_DATE_PATTERN);

		validator.initialize(date);
		assertTrue(validator.isValid(PAST_DATE_STRING, context));
	}

	@Test
	public void should_report_date_as_valid_when_correct_in_future()
	{
		when(date.pattern()).thenReturn(ISO8601_DATE_PATTERN);

		validator.initialize(date);
		assertTrue(validator.isValid(FAR_FUTURE_DATE_STRING, context));
	}

	@Test
	public void should_report_date_as_valid_when_null()
	{
		when(date.pattern()).thenReturn(ISO8601_DATE_PATTERN);

		validator.initialize(date);
		assertTrue(validator.isValid(null, context));
	}

	@Test
	public void should_report_date_as_invalid_when_invalid_date()
	{
		when(date.pattern()).thenReturn(ISO8601_DATE_PATTERN);
		when(context.getDefaultConstraintMessageTemplate()).thenReturn("must match ");
		when(context.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(constraintViolationBuilder);

		validator.initialize(date);
		assertFalse(validator.isValid(INVALID_DATE_STRING, context));

		verify(context).buildConstraintViolationWithTemplate(contains("must match "));
		verify(constraintViolationBuilder).addConstraintViolation();
	}

	@Test
	public void should_report_date_as_invalid()
	{
		when(date.pattern()).thenReturn(ISO8601_DATE_PATTERN);
		when(context.getDefaultConstraintMessageTemplate()).thenReturn("must match ");
		when(context.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(constraintViolationBuilder);

		validator.initialize(date);
		assertFalse(validator.isValid(INVALID_DATE_STRING, context));

		verify(context).buildConstraintViolationWithTemplate(anyString());
		verify(constraintViolationBuilder).addConstraintViolation();
	}

	@Test
	public void should_throw_exception_when_using_invalid_pattern()
	{
		when(date.pattern()).thenReturn(ISO8601_DATE_PATTERN);

		assertThrows(IllegalArgumentException.class, () -> {
			when(date.pattern()).thenReturn(INVALID_DATE_PATTERN);

			validator.initialize(date);
		});
	}

	@Test
	public void should_throw_exception_when_using_null_pattern()
	{
		when(date.pattern()).thenReturn(ISO8601_DATE_PATTERN);

		assertThrows(NullPointerException.class, () -> {
			when(date.pattern()).thenReturn(null);

			validator.initialize(date);
		});
	}

}
