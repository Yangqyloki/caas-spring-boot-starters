package com.hybris.caas.web.sort;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define the valid {@link org.springframework.data.domain.Sort} properties to be used when injecting a
 * {@link org.springframework.data.domain.Sort} instance into a controller handler method.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SortProperties
{
	/**
	 * Returns the valid property names to be used by this sort parameter.
	 *
	 * @return array of supported sorting property names.
	 */
	String[] value();

	/**
	 * Provides the list of column name matching sort parameter property names.
	 *
	 * @return array of the database column supported matching property names.
	 */
	String[] column() default {};
}
