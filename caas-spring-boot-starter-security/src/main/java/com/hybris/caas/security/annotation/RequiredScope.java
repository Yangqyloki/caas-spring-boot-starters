package com.hybris.caas.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used on fields to specify that they should only be serialized if the security context has the required scope
 */

@Documented
@Retention(RUNTIME)
public @interface RequiredScope
{
	String[] value();

}
