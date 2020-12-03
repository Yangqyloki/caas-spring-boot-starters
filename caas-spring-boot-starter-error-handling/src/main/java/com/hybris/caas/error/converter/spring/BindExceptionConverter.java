package com.hybris.caas.error.converter.spring;

import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import com.hybris.caas.error.ErrorMessage;

/**
 *
 * Convert a {@link BindException} to a {@link ErrorMessage}.
 *
 * @see AbstractBindingResultBasedExceptionConverter
 */
public class BindExceptionConverter extends AbstractBindingResultBasedExceptionConverter<BindException>
{

	@Override
	protected BindingResult getBindingResult(BindException ex)
	{
		return ex.getBindingResult();
	}

}
