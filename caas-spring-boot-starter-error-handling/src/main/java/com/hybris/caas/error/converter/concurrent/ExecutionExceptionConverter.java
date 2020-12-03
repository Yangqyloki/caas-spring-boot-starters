package com.hybris.caas.error.converter.concurrent;

import com.hybris.caas.error.converter.AbstractCauseExceptionConverter;

import java.util.concurrent.ExecutionException;

/**
 * Converts {@link ExecutionException} to CaaS error message.
 */
public class ExecutionExceptionConverter extends AbstractCauseExceptionConverter<ExecutionException>
{
	@Override
	protected boolean useRootCause()
	{
		return true;
	}
}
