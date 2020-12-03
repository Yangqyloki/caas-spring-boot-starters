package com.hybris.caas.kafka.interceptor;

import com.hybris.caas.kafka.config.CaasKafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static com.hybris.caas.kafka.util.CaasKafkaConstants.LONG_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.SHORT_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.RetryableConsumerUtils.getRetryableConsumerByTopicName;

/**
 * Interceptor for {@link ConsumerRecord} invoked by the listener container before invoking the listener.
 * Enforces the required message delay when consuming records from retry topics by throwing {@link DelayException}
 * when the required delay has not already passed.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class RetryableConsumerRecordInterceptor<K, V> implements RecordInterceptor<K, V>
{
	private static final Logger LOG = LoggerFactory.getLogger(RetryableConsumerRecordInterceptor.class);

	private final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap;

	private final long idleBetweenRetryPollsMs;

	public RetryableConsumerRecordInterceptor(final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap,
			final long idleBetweenRetryPollsMs)
	{
		Assert.isTrue(idleBetweenRetryPollsMs > 0, "idleBetweenRetryPollsMs must be a positive value");

		this.retryableConsumerMap = retryableConsumerMap;
		this.idleBetweenRetryPollsMs = idleBetweenRetryPollsMs;
	}

	@Override
	public ConsumerRecord<K, V> intercept(final ConsumerRecord<K, V> record)
	{
		final String recordTopicName = record.topic();

		LOG.debug("Received message with timestamp {} for topic {}.", record.timestamp(), recordTopicName);

		if (!(recordTopicName.endsWith(SHORT_DELAY_RETRY_TOPIC_SUFFIX) || recordTopicName.endsWith(LONG_DELAY_RETRY_TOPIC_SUFFIX)))
		{
			return record;
		}

		return interceptRetry(record);
	}

	private ConsumerRecord<K, V> interceptRetry(final ConsumerRecord<K, V> record)
	{
		getRetryableConsumerByTopicName(retryableConsumerMap, record).ifPresent(retryableConsumer -> {
			long requiredDelayMs = 0;

			if (record.topic().endsWith(SHORT_DELAY_RETRY_TOPIC_SUFFIX) && Objects.nonNull(retryableConsumer.getShortDelayRetry()))
			{
				requiredDelayMs = retryableConsumer.getShortDelayRetry().getMessageDelayMs();
			}
			else if (record.topic().endsWith(LONG_DELAY_RETRY_TOPIC_SUFFIX) && Objects.nonNull(retryableConsumer.getLongDelayRetry()))
			{
				requiredDelayMs = retryableConsumer.getLongDelayRetry().getMessageDelayMs();
			}

			final long currentTimestamp = Instant.now().toEpochMilli();
			final long messageTimestamp = record.timestamp();

			final long pastMs = currentTimestamp - (messageTimestamp + requiredDelayMs);

			if (pastMs < 0)
			{
				final long positivePastMs = -pastMs;
				if (positivePastMs <= idleBetweenRetryPollsMs)
				{
					sleep(positivePastMs);
				}
				else
				{
					sleep(idleBetweenRetryPollsMs);

					throw new DelayException(-(pastMs + idleBetweenRetryPollsMs));
				}
			}
		});

		return record;
	}

	private void sleep(final long durationMs)
	{
		try
		{
			Thread.sleep(durationMs);
		}
		catch (final InterruptedException e)
		{
			Thread.currentThread().interrupt();

			// ensure delay is enforced
			throw new DelayException("Thread sleep interrupted.", e);
		}
	}
}
