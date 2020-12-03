package com.hybris.caas.web.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.hybris.caas.web.interceptor.UriLengthValidationInterceptor.DEFAULT_MAX_URI_LENGTH;

/**
 * Annotation that indicates that URI length validation should be applied.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UriLengthValidation
{
	/**
	 * The maximum URI length supported.
	 *
	 * @return the maximum URI length supported or the default value.
	 */
	int maxLength() default DEFAULT_MAX_URI_LENGTH;
}
