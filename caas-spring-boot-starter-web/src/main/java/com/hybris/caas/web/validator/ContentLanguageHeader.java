package com.hybris.caas.web.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

@Documented
@Target({ PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContentLanguageHeaderValidator.class)
public @interface ContentLanguageHeader
{
	String message() default "Invalid 'Content-Language' request header.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
