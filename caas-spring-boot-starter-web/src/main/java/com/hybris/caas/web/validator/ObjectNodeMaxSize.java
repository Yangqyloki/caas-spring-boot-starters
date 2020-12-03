package com.hybris.caas.web.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Validates the max size of the json object. This checks the number of characters, escape characters are not included.
 */
@Documented
@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ObjectNodeMaxSizeValidator.class)
public @interface ObjectNodeMaxSize
{
	/**
	 * The maximum length of the json object.
	 *
	 * @return the maximum length of the json object<code>32768</code>
	 */
	int max() default 32768;

	String message() default "The json object provided is too large. The maximum size is %s characters.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
