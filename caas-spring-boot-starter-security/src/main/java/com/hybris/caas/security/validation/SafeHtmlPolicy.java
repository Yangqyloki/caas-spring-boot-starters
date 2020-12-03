package com.hybris.caas.security.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Defines a custom annotation for validating HTML content according to a policy.
 */
@Documented
@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {SafeHtmlPolicyValidator.class, SafeHtmlMapPolicyValidator.class})
public @interface SafeHtmlPolicy
{
	String message() default "String contents contains unsafe HTML components.";

	/**
	 * The name of the HTML policy to apply during the validation. This name
	 * should match the name of a spring bean of type {@link com.hybris.caas.security.sanitization.HtmlPolicyFactory}.
	 *
	 * @return the policy name
	 */
	String policy();

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
