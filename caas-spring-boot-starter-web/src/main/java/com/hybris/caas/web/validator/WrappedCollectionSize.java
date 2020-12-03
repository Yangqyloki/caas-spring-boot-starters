package com.hybris.caas.web.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Defines a custom annotation for validating a {@link com.hybris.caas.web.WrappedCollection}'s size.
 */
@Documented
@Target({ PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = WrappedCollectionSizeValidator.class)
public @interface WrappedCollectionSize
{
	String message() default "{javax.validation.constraints.Size.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return size the element must be higher or equal to
	 */
	int min() default 0;

	/**
	 * @return size the element must be lower or equal to
	 */
	int max() default Integer.MAX_VALUE;

}
