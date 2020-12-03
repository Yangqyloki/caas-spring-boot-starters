package com.hybris.caas.kafka.error;

/**
 * Exception to be thrown when kafka server is unreachable.
 */
public class KafkaRuntimeException extends RuntimeException
{
	public KafkaRuntimeException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
