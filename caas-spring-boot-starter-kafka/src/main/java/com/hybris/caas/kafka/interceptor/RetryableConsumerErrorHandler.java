package com.hybris.caas.kafka.interceptor;

import com.hybris.caas.kafka.error.RetryableConsumerDeadLetterPublishingRecoverer;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.log.LogAccessor;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.listener.SeekUtils;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Error handler to be used for retryable consumers that allows {@link DelayException} propagation.
 * When {@link DelayException} is being handled, seek records to earliest position is being done for the current record
 * as well as for the remaining unconsumed records.
 * This is being done by using {@link SeekUtils#doSeeks(List, Consumer, Exception, boolean, BiPredicate, LogAccessor)}.
 * <p>
 * If any other error is being handled, the error handler forwards the call to {@link SeekToCurrentErrorHandler} to handle the exception.
 * However, the {@link SeekToCurrentErrorHandler} is not configured with a {@link org.springframework.kafka.listener.DeadLetterPublishingRecoverer}.
 * <p>
 * A {@link org.springframework.kafka.listener.DeadLetterPublishingRecoverer} is not being provided to {@link SeekToCurrentErrorHandler}
 * in order to allow a possible exception that could occur during publishing to not just being logged as done by {@link SeekToCurrentErrorHandler}
 * that also relies on {@link SeekUtils#doSeeks(List, Consumer, Exception, boolean, BiPredicate, LogAccessor)} but actually allowing it to
 * propagate further to trigger a rollback and being handled by {@link org.springframework.kafka.listener.AfterRollbackProcessor}.
 * <p>
 * {@link RetryableConsumerDeadLetterPublishingRecoverer} is being used in order to manipulate the DLT headers that are being added to
 * the published message as well as the timestamp header to reflect the current time when publishing it is being done.
 */
public class RetryableConsumerErrorHandler implements ContainerAwareErrorHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(RetryableConsumerErrorHandler.class);
	private static final LogAccessor LOGGER = new LogAccessor(LogFactory.getLog(RetryableConsumerErrorHandler.class));

	private final SeekToCurrentErrorHandler seekToCurrentErrorHandler;
	private final RetryableConsumerDeadLetterPublishingRecoverer retryableConsumerDeadLetterPublishingRecoverer;

	public RetryableConsumerErrorHandler(
			final RetryableConsumerDeadLetterPublishingRecoverer retryableConsumerDeadLetterPublishingRecoverer)
	{
		this.retryableConsumerDeadLetterPublishingRecoverer = retryableConsumerDeadLetterPublishingRecoverer;
		this.seekToCurrentErrorHandler = new SeekToCurrentErrorHandler((record, ex) -> {
			if (LOG.isDebugEnabled())
			{
				LOG.debug(String.format("Retryable consumer error handler recoverer invoked for %s", record), ex);
			}
		}, new FixedBackOff(0, 0L));
	}

	@Override
	public void handle(final Exception thrownException, final List<ConsumerRecord<?, ?>> records, final Consumer<?, ?> consumer)
	{
		throw new UnsupportedOperationException("Container should never call this handle method");
	}

	@Override
	public void handle(final Exception thrownException, final List<ConsumerRecord<?, ?>> records, final Consumer<?, ?> consumer,
			final MessageListenerContainer container)
	{
		final Throwable cause = NestedExceptionUtils.getMostSpecificCause(thrownException);
		if (cause instanceof DelayException)
		{
			SeekUtils.doSeeks(records, consumer, thrownException, false, (t, u) -> true, LOGGER);

			throw (DelayException) cause;
		}

		final Optional<ConsumerRecord<?, ?>> firstRecord = Optional.ofNullable(records.isEmpty() ? null : records.get(0));

		seekToCurrentErrorHandler.handle(thrownException, records, consumer, container);

		firstRecord.ifPresent(
				consumerRecord -> retryableConsumerDeadLetterPublishingRecoverer.accept(consumerRecord, thrownException));
	}
}
