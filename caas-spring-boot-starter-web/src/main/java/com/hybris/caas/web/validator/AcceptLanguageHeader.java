package com.hybris.caas.web.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a custom annotation to select a locale to be used as the default.
 */
@Documented
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AcceptLanguageHeaderValidator.class)
public @interface AcceptLanguageHeader
{
	boolean allowWildcard() default true;
	boolean lenient() default false;

	String message() default "Invalid 'Accept-Language' request header.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
