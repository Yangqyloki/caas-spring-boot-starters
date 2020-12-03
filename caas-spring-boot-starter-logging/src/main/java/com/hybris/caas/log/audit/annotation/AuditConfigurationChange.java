package com.hybris.caas.log.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to indicate that a service method should perform audit logging for a configuration change.
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface AuditConfigurationChange
{
	/**
	 * The name of the parameter matching the argument which contains the audited object's object id.
	 *
	 * @return the object id parameter name
	 */
	String objectId() default "id";

	/**
	 * The name of the parameter matching the argument which contains the old value of the configuration.
	 *
	 * @return the old value parameter name
	 */
	String oldValue() default "oldValue";
}
