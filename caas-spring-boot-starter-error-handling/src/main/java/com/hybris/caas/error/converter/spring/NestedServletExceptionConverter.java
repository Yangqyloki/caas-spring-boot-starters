package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractCauseExceptionConverter;
import org.springframework.web.util.NestedServletException;

/**
 * Convert a {@link NestedServletException} to a {@link ErrorMessage}.
 */
public class NestedServletExceptionConverter extends AbstractCauseExceptionConverter<NestedServletException>
{
	@Override
	protected boolean useRootCause()
	{
		return true;
	}
}
