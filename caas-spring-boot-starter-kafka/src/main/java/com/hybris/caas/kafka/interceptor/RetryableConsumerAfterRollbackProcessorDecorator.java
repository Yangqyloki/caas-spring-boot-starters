package com.hybris.caas.kafka.interceptor;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.kafka.listener.AfterRollbackProcessor;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.List;

/**
 * Decorates {@link AfterRollbackProcessor} by performing an exception check during 
 * {@link AfterRollbackProcessor#process(List, Consumer, Exception, boolean, ContainerProperties.EOSMode)}
 * call before delegating the call. Method call delegation does not take place for {@link DelayException}.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class RetryableConsumerAfterRollbackProcessorDecorator<K, V> implements AfterRollbackProcessor<K, V>
{
	private final AfterRollbackProcessor<K, V> afterRollbackProcessor;

	public RetryableConsumerAfterRollbackProcessorDecorator(final AfterRollbackProcessor<K, V> afterRollbackProcessor)
	{
		this.afterRollbackProcessor = afterRollbackProcessor;
	}

	@Override
	public void process(final List<ConsumerRecord<K, V>> consumerRecords, final Consumer<K, V> consumer, final Exception exception,
			final boolean recoverable)
	{
		process(consumerRecords, consumer, exception, recoverable, ContainerProperties.EOSMode.ALPHA);
	}

	@Override
	public void process(final List<ConsumerRecord<K, V>> consumerRecords, final Consumer<K, V> consumer, final Exception exception,
			final boolean recoverable, final ContainerProperties.EOSMode eosMode)
	{
		final Throwable cause = NestedExceptionUtils.getMostSpecificCause(exception);

		if (!(cause instanceof DelayException))
		{
			afterRollbackProcessor.process(consumerRecords, consumer, exception, recoverable, eosMode);
		}
	}

	@Override
	public void clearThreadState()
	{
		afterRollbackProcessor.clearThreadState();
	}

	@Override
	public boolean isProcessInTransaction()
	{
		return afterRollbackProcessor.isProcessInTransaction();
	}

	public AfterRollbackProcessor<K, V> getAfterRollbackProcessor()
	{
		return this.afterRollbackProcessor;
	}
}
