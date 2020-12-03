package com.hybris.caas.error.exception;

/**
 * Exception thrown when too many requests is done for a tenant in a given interval.
 */
public class TooManyRequestsException extends RuntimeException
{
	private static final long serialVersionUID = 1622224817635042312L;
	private static final String MESSAGE = "The service only allows %d requests per %s to this endpoint. A Retry-After response header will indicate how long to wait before making a new request.";

	private final long retryAfter;

	public TooManyRequestsException(final long requestAllowed, final String period, final long retryAfter)
	{
		super(String.format(MESSAGE, requestAllowed, period));
		this.retryAfter = retryAfter;
	}

	public long getRetryAfter()
	{
		return retryAfter;
	}
}

