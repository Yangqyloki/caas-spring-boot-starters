package com.hybris.caas.web.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.hybris.caas.data.utils.DateUtils.ISO8601_DATE_PATTERN;
import static java.lang.annotation.ElementType.FIELD;

/**
 * Defines a custom annotation for validating a date.
 */
@Documented
@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateValidator.class)
public @interface Date
{

	String message() default "must match ";

	/**
	 * The default date pattern to be used to validate a date.
	 *
	 * @return the user provided date pattern or the default date pattern: ISO8601
	 */
	String pattern() default ISO8601_DATE_PATTERN;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
