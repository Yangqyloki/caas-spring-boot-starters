package com.hybris.caas.kafka.transaction;

import com.hybris.caas.kafka.config.CaasKafkaProperties;
import com.hybris.caas.kafka.error.KafkaRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SyncKafkaTemplateTest
{
	@Mock
	private KafkaTemplate<?, ?> kafkaTemplate;
	@Mock
	private Message<String> message;
	@Mock
	private ListenableFuture future;

	final CaasKafkaProperties properties = new CaasKafkaProperties();

	private SyncKafkaTemplate syncKafkaTemplate;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp()
	{
		when(kafkaTemplate.send(message)).thenReturn(future);

		syncKafkaTemplate = new SyncKafkaTemplate(kafkaTemplate, properties);
	}

	@Test
	public void shouldSendMessage() throws ExecutionException, InterruptedException, TimeoutException
	{
		syncKafkaTemplate.send(message);

		verify(kafkaTemplate).send(message);
		verify(future).get(properties.getProducerSendTimeoutMs(), TimeUnit.MILLISECONDS);
	}

	@Test(expected = KafkaRuntimeException.class)
	public void shouldFail_InterruptException() throws ExecutionException, InterruptedException, TimeoutException
	{
		when(future.get(properties.getProducerSendTimeoutMs(), TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());

		syncKafkaTemplate.send(message);
	}

	@Test(expected = KafkaRuntimeException.class)
	public void shouldFail_TimeoutException() throws ExecutionException, InterruptedException, TimeoutException
	{
		when(future.get(properties.getProducerSendTimeoutMs(), TimeUnit.MILLISECONDS)).thenThrow(new TimeoutException());

		syncKafkaTemplate.send(message);
	}
}
