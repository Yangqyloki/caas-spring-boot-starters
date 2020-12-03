package com.hybris.caas.kafka.transaction;

import com.hybris.caas.kafka.config.CaasKafkaProperties;
import com.hybris.caas.kafka.error.KafkaRuntimeException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Synchronous kafka template, wraps any exception resulting from sending a message into a {@link KafkaRuntimeException}
 */
public class SyncKafkaTemplate
{
	private final KafkaTemplate<?, ?> kafkaTemplate;
	private final long timeout;

	public SyncKafkaTemplate(final KafkaTemplate<?, ?> kafkaTemplate, final CaasKafkaProperties caasKafkaProperties)
	{
		this.kafkaTemplate = kafkaTemplate;
		this.timeout = caasKafkaProperties.getProducerSendTimeoutMs();
	}

	/**
	 * Sends a {@link Message} to the Kafka broker(s)
	 *
	 * @param message to be sent
	 * @throws KafkaRuntimeException when fail to send kafka message
	 */
	public void send(Message<?> message)
	{

		final ListenableFuture<? extends SendResult<?, ?>> future = kafkaTemplate.send(message);

		try
		{
			// if an error happens, an exception would be thrown after transaction/metadata timeout has elapsed
			future.get(timeout, TimeUnit.MILLISECONDS);
		}
		catch (final InterruptedException e)
		{
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			throw new KafkaRuntimeException("An error occurred while sending the kafka message", e);
		}
		catch (final ExecutionException | TimeoutException e)
		{
			throw new KafkaRuntimeException("An error occurred while sending the kafka message", e);
		}
	}

}
