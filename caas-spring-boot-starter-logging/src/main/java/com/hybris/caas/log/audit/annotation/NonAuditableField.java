package com.hybris.caas.log.audit.annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used on entity attributes to indicate that they should not be serialized. When used in conjunction with a Jackson ObjectMapper
 * containing an {@code AuditLoggingIntrospector}, the field is not serialized.
 */
@Documented
@Retention(RUNTIME)
public @interface NonAuditableField
{
	// Nothing to add here
}
