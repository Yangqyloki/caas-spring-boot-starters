package com.hybris.caas.web.pagination;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define the maximum page size allowed for a {@link org.springframework.data.domain.Pageable} argument in a controller handler method.
 * This is not a validator.; if the size provided is larger than the max size provided, then the handler should use the max size provided instead.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MaxPageSize
{
	/**
	 * Returns the maximum page size allowed for a given controller injecting a {@link org.springframework.data.domain.Pageable} argument.
	 *
	 * @return the max page size.
	 */
	int value();
}
