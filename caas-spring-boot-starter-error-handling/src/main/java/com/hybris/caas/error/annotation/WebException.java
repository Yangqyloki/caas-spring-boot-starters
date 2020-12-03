package com.hybris.caas.error.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.http.HttpStatus;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;

/**
 * Annotation to indicate that this exception should be converted to a
 * {@link ErrorMessage} with the appropriate attributes. The
 * <code>message</code> attribute of the error message will be populated with
 * the actual exception's message. All other attributes will be populated by the
 * annotations parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebException
{
	/**
	 * The type will be mapped to the <code>type</code> attribute of the error message.
	 *
	 * @return the type
	 */
	public String type();

	/**
	 * The status will be mapped to the <code>status</code> attribute of the error message.
	 *
	 * @return the status code
	 */
	public HttpStatus status();

	/**
	 * The info will be mapped to the <code>moreInfo</code> attribute of the error message.
	 * @return more info
	 */
	public String info() default ErrorConstants.INFO;

}
