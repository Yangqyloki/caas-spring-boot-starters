package com.hybris.caas.log.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to indicate that a service method should perform audit logging for a security event.
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface AuditSecurityEvent
{
	// No configuration yet.
}
