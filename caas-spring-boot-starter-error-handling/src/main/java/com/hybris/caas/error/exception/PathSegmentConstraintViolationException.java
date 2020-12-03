package com.hybris.caas.error.exception;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * Exception thrown when a path segments is invalid (i.e. invalid format).
 */
public class PathSegmentConstraintViolationException extends ConstraintViolationException
{
	public PathSegmentConstraintViolationException(Set<? extends ConstraintViolation<?>> constraintViolations)
	{
		super(constraintViolations);
	}
}
