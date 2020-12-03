package com.hybris.caas.kafka.transaction;

import com.hybris.caas.kafka.error.KafkaRuntimeException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChainedKafkaTransactionAspectTest
{
	@Mock
	private DefaultKafkaProducerFactory producerFactory;

	private ChainedKafkaTransactionAspect aspect;

	@Before
	public void setUp()
	{
		aspect = new ChainedKafkaTransactionAspect(producerFactory);
	}

	@Test
	public void shouldDestroyKafkaProducersOnProducerFencedException() throws Exception
	{
		aspect.handleException(kafkaRuntimeException(new ProducerFencedException("some root cause")));
		verify(producerFactory).destroy();
	}

	@Test
	public void shouldDestroyKafkaProducersOnOutOfOrderSequenceException() throws Exception
	{
		aspect.handleException(kafkaRuntimeException(new OutOfOrderSequenceException("some root cause")));
		verify(producerFactory).destroy();
	}

	@Test
	public void shouldDestroyKafkaProducersOnAuthorizationException() throws Exception
	{
		aspect.handleException(kafkaRuntimeException(new AuthorizationException("some root cause")));
		verify(producerFactory).destroy();
	}

	@Test
	public void shouldNotDestroyKafkaProducers() throws Exception
	{
		aspect.handleException(kafkaRuntimeException(new Exception("any exception")));
		verify(producerFactory, never()).destroy();
	}

	private static KafkaRuntimeException kafkaRuntimeException(final Exception rootCause)
	{
		return new KafkaRuntimeException("some failure", rootCause);
	}

}
