package com.hybris.caas.web.validator;

import com.hybris.caas.web.Constants;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Defines a custom annotation for validating an identifier.
 */
@Documented
@Target({ PARAMETER, FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IdentifierValidator.class)
public @interface Identifier
{
	String message() default "Invalid identifier.";

	/**
	 * A regular expression to be used to validate the identifier.
	 *
	 * @return the user provided regex or the default regex (a UUID): <code>^[a-fA-F0-9-]{36}$</code>
	 */
	String regex() default Constants.IDENTIFIER_REG_EXP;

	/**
	 * The minimum length of the identifier.
	 *
	 * @return the minimum length of the identifier or the default <code>0</code>
	 */
	int min() default 0;

	/**
	 * The maximum length of the identifier.
	 *
	 * @return the maximum length of the identifier or the default <code>255</code>
	 */
	int max() default 255;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
