package com.hybris.caas.kafka.transaction;

import com.hybris.caas.kafka.error.KafkaRuntimeException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

/**
 * Aspect to close kafka producers following a un-recoverable kafka exception.<br>
 * Order is set to be run after transaction has completed.<br>
 * see com.hybris.caas.multitenant.jpa.config.JpaMultitenantConfig for Order<br>
 * see {@link KafkaProducer} for un-recoverable exceptions<br>
 */
@Aspect
@Order(50)
public class ChainedKafkaTransactionAspect
{
	private static final Logger LOG = LoggerFactory.getLogger(ChainedKafkaTransactionAspect.class);

	private final DefaultKafkaProducerFactory producerFactory;

	public ChainedKafkaTransactionAspect(final DefaultKafkaProducerFactory producerFactory)
	{
		this.producerFactory = producerFactory;
	}

	@AfterThrowing(pointcut = "@annotation(com.hybris.caas.kafka.transaction.ChainedKafkaTransactional)", throwing = "ex")
	public void handleException(final KafkaRuntimeException ex)
	{
		final Throwable cause = NestedExceptionUtils.getMostSpecificCause(ex);

		if (cause instanceof ProducerFencedException || cause instanceof OutOfOrderSequenceException
				|| cause instanceof AuthorizationException)
		{
			try
			{
				// needs to be called after the transaction has completed so it does close the kafka producers
				LOG.warn("Destroying kafka producer instances.");
				producerFactory.destroy();
			}
			catch (final Exception e)
			{
				// not throwing this exception, propagating the exception received as argument
				LOG.error("Failed to destroy kafka producers", e);
			}
		}
	}

}
