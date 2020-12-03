package com.hybris.caas.error.annotation;

import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ignore exceptions due to unique constraint violations.
 * Ignore the {@link TransactionSystemException} or {@link JpaSystemException}
 * whose root cause message contains at least one of the strings:
 * <ul>
 * <li>unique</li>
 * <li>duplicate key</li>
 * <li>primary key violation</li>
 * </ul>
 */
@IgnoreException(value = { TransactionSystemException.class, JpaSystemException.class }, rootCauseMessageFilter = { "unique",
		"duplicate key", "primary key violation" })
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreUniqueConstraint
{
}
