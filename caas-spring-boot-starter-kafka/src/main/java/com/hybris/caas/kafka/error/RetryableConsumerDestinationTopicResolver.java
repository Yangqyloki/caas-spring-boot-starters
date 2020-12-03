package com.hybris.caas.kafka.error;

import com.hybris.caas.kafka.config.CaasKafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.hybris.caas.kafka.util.CaasKafkaConstants.DEAD_LETTER_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.LONG_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.SHORT_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.RetryableConsumerUtils.getOriginalTopic;
import static com.hybris.caas.kafka.util.RetryableConsumerUtils.getRetryableConsumerByTopicName;

/**
 * Determines the destination topic for the failed message.
 * For a retryable consumer, a message that keeps failing the processing follows the pattern captured below:
 * topic -> short delay retry topic -> long delay retry topic -> dead letter topic
 * <p>
 * However, different patterns are supported as well based on requirements reflected into the configuration of the retryable consumer:
 * topic -> dead letter topic | topic -> short delay retry topic |  topic -> short delay retry topic -> long delay retry topic
 * <p>
 * Note: In case a dead letter topic is not being used, the message would be retried 2 more times and then discarded (logged).
 * Please see {@link com.hybris.caas.kafka.config.CaasKafkaConfig} retryableKafkaListenerContainerFactory bean for more details.
 */
public class RetryableConsumerDestinationTopicResolver
{
	private static final Logger LOG = LoggerFactory.getLogger(RetryableConsumerDestinationTopicResolver.class);

	private static final String NO_FURTHER_RECOVERING_CONFIG_FOR_TOPIC = "No further recovering configuration defined for topic: %s";

	private final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap;

	public RetryableConsumerDestinationTopicResolver(final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap)
	{
		this.retryableConsumerMap = retryableConsumerMap;
	}

	public TopicPartition getTopicPartition(final ConsumerRecord<?, ?> record, final Exception exception)
	{
		final String recordTopicName = record.topic();
		final String originalTopic = getOriginalTopic(record);

		final CaasKafkaProperties.RetryableConsumer retryableConsumer = getRetryableConsumerByTopicName(retryableConsumerMap,
				originalTopic).orElseThrow(
				() -> new IllegalStateException(String.format("No retryable-consumer found for topic: %s", recordTopicName),
						exception));

		final Supplier<KafkaRuntimeException> exceptionSupplier = () -> new KafkaRuntimeException(
				String.format(NO_FURTHER_RECOVERING_CONFIG_FOR_TOPIC, recordTopicName), exception);

		final TopicPartition targetTopicPartition;

		if (recordTopicName.endsWith(SHORT_DELAY_RETRY_TOPIC_SUFFIX))
		{
			final String topicName = getTargetTopicNameForShortRetryTopic(originalTopic, retryableConsumer, exceptionSupplier);
			targetTopicPartition = new TopicPartition(topicName, -1);
		}
		else if (recordTopicName.endsWith(LONG_DELAY_RETRY_TOPIC_SUFFIX))
		{
			final String topicName = getTargetTopicNameForLongRetryTopic(originalTopic, retryableConsumer, exceptionSupplier);
			return new TopicPartition(topicName, -1);
		}
		else if (recordTopicName.endsWith(DEAD_LETTER_TOPIC_SUFFIX))
		{
			throw new UnsupportedOperationException(
					String.format("Consuming messages from dead letter topic not supported. Topic: %s", recordTopicName));
		}
		else
		{
			final String topicName = getTargetTopicNameForRegularTopic(originalTopic, retryableConsumer, exceptionSupplier);
			targetTopicPartition = new TopicPartition(topicName, -1);
		}

		final String targetTopicName = targetTopicPartition.topic();
		LOG.debug("Target topic {} selected for topic {}.", targetTopicName, recordTopicName);

		return targetTopicPartition;
	}

	private <X extends Throwable> String getTargetTopicNameForRegularTopic(final String originalTopicName,
			final CaasKafkaProperties.RetryableConsumer retryableConsumer, Supplier<? extends X> exceptionSupplier) throws X
	{
		final Optional<CaasKafkaProperties.RetryTopic> shortDelayRetry = Optional.ofNullable(retryableConsumer.getShortDelayRetry());
		final Optional<CaasKafkaProperties.RetryTopic> longDelayRetry = Optional.ofNullable(retryableConsumer.getLongDelayRetry());
		final Optional<CaasKafkaProperties.DeadLetterTopic> deadLetter = Optional.ofNullable(retryableConsumer.getDeadLetter());

		if (shortDelayRetry.isPresent())
		{
			return shortDelayRetry.map(CaasKafkaProperties.RetryTopic::getTopicPrefix).orElse(originalTopicName)
					+ SHORT_DELAY_RETRY_TOPIC_SUFFIX;
		}
		else if (longDelayRetry.isPresent())
		{
			return longDelayRetry.map(CaasKafkaProperties.RetryTopic::getTopicPrefix).orElse(originalTopicName)
					+ LONG_DELAY_RETRY_TOPIC_SUFFIX;
		}
		else if (deadLetter.isPresent())
		{
			return deadLetter.map(CaasKafkaProperties.DeadLetterTopic::getTopicPrefix).orElse(originalTopicName)
					+ DEAD_LETTER_TOPIC_SUFFIX;
		}
		else
		{
			throw exceptionSupplier.get();
		}
	}

	private <X extends Throwable> String getTargetTopicNameForShortRetryTopic(final String originalTopicName,
			final CaasKafkaProperties.RetryableConsumer retryableConsumer, Supplier<? extends X> exceptionSupplier) throws X
	{
		final Optional<CaasKafkaProperties.RetryTopic> longDelayRetry = Optional.ofNullable(retryableConsumer.getLongDelayRetry());
		final Optional<CaasKafkaProperties.DeadLetterTopic> deadLetter = Optional.ofNullable(retryableConsumer.getDeadLetter());

		if (longDelayRetry.isPresent())
		{
			return longDelayRetry.map(CaasKafkaProperties.RetryTopic::getTopicPrefix).orElse(originalTopicName)
					+ LONG_DELAY_RETRY_TOPIC_SUFFIX;
		}
		else if (deadLetter.isPresent())
		{
			return deadLetter.map(CaasKafkaProperties.DeadLetterTopic::getTopicPrefix).orElse(originalTopicName)
					+ DEAD_LETTER_TOPIC_SUFFIX;
		}
		else
		{
			throw exceptionSupplier.get();
		}
	}

	private <X extends Throwable> String getTargetTopicNameForLongRetryTopic(final String originalTopicName,
			final CaasKafkaProperties.RetryableConsumer retryableConsumer, Supplier<? extends X> exceptionSupplier) throws X
	{
		final Optional<CaasKafkaProperties.DeadLetterTopic> deadLetter = Optional.ofNullable(retryableConsumer.getDeadLetter());
		if (deadLetter.isPresent())
		{
			return deadLetter.map(CaasKafkaProperties.DeadLetterTopic::getTopicPrefix).orElse(originalTopicName)
					+ DEAD_LETTER_TOPIC_SUFFIX;
		}
		else
		{
			throw exceptionSupplier.get();
		}
	}
}
