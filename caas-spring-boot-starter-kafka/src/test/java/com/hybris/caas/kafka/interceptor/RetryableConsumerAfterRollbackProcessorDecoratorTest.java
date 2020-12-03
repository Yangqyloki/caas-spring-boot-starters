package com.hybris.caas.kafka.interceptor;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.listener.AfterRollbackProcessor;
import org.springframework.kafka.listener.ListenerExecutionFailedException;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.kafka.listener.ContainerProperties.EOSMode.ALPHA;

@RunWith(MockitoJUnitRunner.class)
public class RetryableConsumerAfterRollbackProcessorDecoratorTest
{
	@Mock
	private AfterRollbackProcessor<Object, Object> afterRollbackProcessor;

	@Mock
	private Consumer<Object, Object> consumer;

	private final List<ConsumerRecord<Object, Object>> consumerRecords = Collections.emptyList();
	private final Exception exception = new ListenerExecutionFailedException("dummy", new DelayException(5));

	private RetryableConsumerAfterRollbackProcessorDecorator<Object, Object> retryableConsumerAfterRollbackProcessorDecorator;

	@Before
	public void setUp()
	{
		retryableConsumerAfterRollbackProcessorDecorator = new RetryableConsumerAfterRollbackProcessorDecorator<>(
				afterRollbackProcessor);
	}

	@Test
	public void should_delegate_isProcessInTransaction()
	{
		retryableConsumerAfterRollbackProcessorDecorator.isProcessInTransaction();

		verify(afterRollbackProcessor).isProcessInTransaction();
	}

	@Test
	public void should_delegate_clearThreadState()
	{
		retryableConsumerAfterRollbackProcessorDecorator.clearThreadState();

		verify(afterRollbackProcessor).clearThreadState();
	}

	@Test
	public void should_handle_DelayException()
	{
		retryableConsumerAfterRollbackProcessorDecorator.process(consumerRecords, consumer, exception, false, ALPHA);

		verifyNoInteractions(afterRollbackProcessor);
	}

	@Test
	public void should_delagate_process_for_non_DelayException()
	{
		final ListenerExecutionFailedException dummyException = new ListenerExecutionFailedException("dummy");

		retryableConsumerAfterRollbackProcessorDecorator.process(consumerRecords, consumer, dummyException, false, ALPHA);

		verify(afterRollbackProcessor).process(consumerRecords, consumer, dummyException, false, ALPHA);
	}
}
