package com.hybris.caas.log.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used on entity attributes to indicate that they contain sensitive data. When used in conjunction with a Jackson ObjectMapper
 * containing an {@code AuditLoggingIntrospector}, the first four and last four characters are kept while the middle characters are converted to asterisks(*)
 */
@Documented
@Retention(RUNTIME)
public @interface PartiallyMaskedAuditField
{
	// Nothing to add here
}
