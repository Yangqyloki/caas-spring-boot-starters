package com.hybris.caas.web.validator;

import com.hybris.caas.data.utils.DateUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Validate the given parameter field against the date pattern provided in the annotation.
 */
public class DateValidator implements ConstraintValidator<Date, String>
{
	private String pattern;
	private DateTimeFormatter dtf;

	@Override
	public void initialize(final Date constraintAnnotation)
	{
		this.pattern = requireNonNull(constraintAnnotation.pattern(), DateUtils.PATTERN_CANNOT_BE_NULL);
		this.dtf = DateUtils.parseDateFormat(pattern);
	}

	@Override
	public boolean isValid(final String date, final ConstraintValidatorContext context)
	{
		if (Objects.isNull(date))
		{
			return true;
		}

		return (parseDate(date, context).isPresent());
	}

	private Optional<OffsetDateTime> parseDate(final String date, final ConstraintValidatorContext context)
	{
		try
		{
			return Optional.of(OffsetDateTime.parse(date, dtf));
		}
		catch (final DateTimeParseException e)
		{
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate() + pattern).addConstraintViolation();

			return Optional.empty();
		}
	}

}
