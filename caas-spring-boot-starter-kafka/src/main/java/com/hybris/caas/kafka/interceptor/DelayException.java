package com.hybris.caas.kafka.interceptor;

/**
 * Exception class used to enforce the required delay when processing messages from a retry topic for a retryable consumer.
 */
class DelayException extends RuntimeException
{
	private static final String ERROR_MESSAGE = "Delay required: %s";

	DelayException(final long requiredDelay)
	{
		super(String.format(ERROR_MESSAGE, requiredDelay));
	}

	DelayException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
