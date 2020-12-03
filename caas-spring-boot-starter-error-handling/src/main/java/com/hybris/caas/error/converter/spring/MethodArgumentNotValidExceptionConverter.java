package com.hybris.caas.error.converter.spring;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.hybris.caas.error.ErrorMessage;

/**
 *
 * Convert a {@link MethodArgumentNotValidException} to a
 * {@link ErrorMessage}.
 *
 * @see AbstractBindingResultBasedExceptionConverter
 */
public class MethodArgumentNotValidExceptionConverter
		extends AbstractBindingResultBasedExceptionConverter<MethodArgumentNotValidException>
{

	@Override
	protected BindingResult getBindingResult(MethodArgumentNotValidException ex)
	{
		return ex.getBindingResult();
	}

}
