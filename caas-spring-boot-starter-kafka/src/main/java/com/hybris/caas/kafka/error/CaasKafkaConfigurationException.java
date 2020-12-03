package com.hybris.caas.kafka.error;

/**
 * Exception to be thrown when an exception occur while setting up kafka configuration.
 */
public class CaasKafkaConfigurationException extends RuntimeException
{
	public CaasKafkaConfigurationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
