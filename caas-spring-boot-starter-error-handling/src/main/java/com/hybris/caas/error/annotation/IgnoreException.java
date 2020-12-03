package com.hybris.caas.error.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ignore exceptions thrown by managed Spring beans' methods
 * It's possible to annotate other annotations to preconfigure some values for reusability
 * Filter exceptions by:
 * <ul>
 * <li>type</li>
 * <li>type and root cause message</li>
 * </ul>
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreException
{
	/**
	 * Exception classes to ignore
	 */
	Class<? extends Exception>[] value();

	/**
	 * Ignore matched exception classes whose root cause message contains any of the given strings
	 */
	String[] rootCauseMessageFilter() default {};
}
