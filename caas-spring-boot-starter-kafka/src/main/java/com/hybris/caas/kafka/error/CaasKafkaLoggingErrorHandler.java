package com.hybris.caas.kafka.error;

import com.hybris.caas.error.converter.ExceptionConverter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ErrorHandler;

/**
 * Handles exception thrown during kafka listener and converts it to a canonical error message.
 */
public class CaasKafkaLoggingErrorHandler extends AbstractKafkaLoggingErrorHandler implements ErrorHandler
{
	public CaasKafkaLoggingErrorHandler(final ExceptionConverter exceptionConverter)
	{
		super(exceptionConverter);
	}

	@Override
	public void handle(final Exception thrownException, final ConsumerRecord<?, ?> record)
	{
		handleException(thrownException, record, this::buildMessageFromConsumerRecord);
	}
}
