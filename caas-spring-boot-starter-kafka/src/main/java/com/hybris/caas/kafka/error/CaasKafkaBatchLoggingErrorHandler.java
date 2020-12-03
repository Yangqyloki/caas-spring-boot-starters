package com.hybris.caas.kafka.error;

import com.hybris.caas.error.converter.ExceptionConverter;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.listener.BatchErrorHandler;

/**
 * Handles exception thrown during kafka listener and converts it to a canonical error message.
 */
public class CaasKafkaBatchLoggingErrorHandler extends AbstractKafkaLoggingErrorHandler implements BatchErrorHandler
{

	public CaasKafkaBatchLoggingErrorHandler(final ExceptionConverter exceptionConverter)
	{
		super(exceptionConverter);
	}

	@Override
	public void handle(final Exception thrownException, final ConsumerRecords<?, ?> data)
	{
		handleException(thrownException, data, this::buildMessageFromConsumerRecords);
	}
}
