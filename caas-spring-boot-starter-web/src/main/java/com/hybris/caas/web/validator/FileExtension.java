package com.hybris.caas.web.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Defines a custom annotation for validating the extension of a file.
 */
@Documented
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileExtensionValidator.class)
public @interface FileExtension
{
	/**
	 * The name of the file extension to be used to build the regex that validates the extension of a given file.
	 *
	 * @return The extension specified in the name property: <code>zip</code>
	 */
	String name();

	String message() default "The file extension must be .%s";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
