package com.hybris.caas.error.converter.concurrent;

import com.hybris.caas.error.converter.AbstractCauseExceptionConverter;

import java.util.concurrent.CompletionException;

/**
 * Converts {@link CompletionException} to CaaS error message.
 */
public class CompletionExceptionConverter extends AbstractCauseExceptionConverter<CompletionException>
{
	@Override
	protected boolean useRootCause()
	{
		return true;
	}
}
