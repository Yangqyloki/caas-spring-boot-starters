package com.hybris.caas.kafka.util;

import com.hybris.caas.kafka.config.CaasKafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.KafkaHeaders;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static com.hybris.caas.kafka.util.CaasKafkaConstants.DEAD_LETTER_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.DOT_SEPARATOR;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.LONG_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.SHORT_DELAY_RETRY_TOPIC_SUFFIX;

/**
 * Defines utility methods used to support retryable consumer functionality.
 */
public final class RetryableConsumerUtils
{
	/**
	 * Replaces an expected suffix within a topic name with another suffix.
	 *
	 * @param topicName         the topic name to have the suffix replaced
	 * @param suffixToReplace   the suffix to replace in the topic name
	 * @param replacementSuffix the replacement suffix
	 * @return the updated topic name having the suffix replaced
	 * @throws IllegalArgumentException if the topic name does not have the expected suffix.
	 */
	public static String replaceTopicSuffix(final String topicName, final String suffixToReplace, final String replacementSuffix)
	{
		if (!topicName.endsWith(suffixToReplace))
		{
			throw new IllegalArgumentException(
					String.format("Topic name %s does not have expected suffix %s", topicName, suffixToReplace));
		}

		final String topicNameWithoutSuffix = topicName.substring(0, (topicName.length() - suffixToReplace.length()));
		return topicNameWithoutSuffix + replacementSuffix;
	}

	/**
	 * Get the original topic name from the kafka header {@link KafkaHeaders#DLT_ORIGINAL_TOPIC} if present,
	 * otherwise the topic name from the consumer record
	 *
	 * @param record the kafka consumer record
	 * @return the original topic name, otherwise the topic name from the consumer record
	 */
	public static String getOriginalTopic(final ConsumerRecord<?, ?> record)
	{
		return Arrays.stream(record.headers().toArray())
				.filter(header -> KafkaHeaders.DLT_ORIGINAL_TOPIC.equals(header.key()))
				.map(header -> new String(header.value()))
				.findFirst()
				.orElse(record.topic());
	}

	/**
	 * Returns {@link CaasKafkaProperties.RetryableConsumer} from configuration map based on the topic name parameter.
	 * A restricted set of suffixes are supported to extract the original topic name.
	 * See {@link CaasKafkaConstants} for the supported set of retry topic suffixes.
	 *
	 * @param retryableConsumerMap the retryable consumer configuration map
	 * @param record               the consumer record to extract the header {@code KafkaHeaders.DLT_ORIGINAL_TOPIC} value otherwise the topic name itself
	 * @return an optional retryable consumer object
	 */
	public static Optional<CaasKafkaProperties.RetryableConsumer> getRetryableConsumerByTopicName(
			final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap, final ConsumerRecord<?, ?> record)
	{
		final String originalTopic = getOriginalTopic(record);

		return getRetryableConsumerByTopicName(retryableConsumerMap, originalTopic);
	}

	/**
	 * Returns {@link CaasKafkaProperties.RetryableConsumer} from configuration map based on the topic name parameter.
	 * A restricted set of suffixes are supported to extract the original topic name.
	 * See {@link CaasKafkaConstants} for the supported set of retry topic suffixes.
	 *
	 * @param retryableConsumerMap the retryable consumer configuration map
	 * @param topicName            the topic name to be used for retrieving retryable consumer from retryable consumer configuration map
	 * @return an optional retryable consumer object
	 */
	public static Optional<CaasKafkaProperties.RetryableConsumer> getRetryableConsumerByTopicName(
			final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap, final String topicName)
	{
		String retryableTopicName = topicName;
		if (retryableTopicName.endsWith(SHORT_DELAY_RETRY_TOPIC_SUFFIX) || retryableTopicName.endsWith(LONG_DELAY_RETRY_TOPIC_SUFFIX)
				|| retryableTopicName.endsWith(DEAD_LETTER_TOPIC_SUFFIX))
		{
			final int dotPosition = retryableTopicName.lastIndexOf(DOT_SEPARATOR);
			retryableTopicName = retryableTopicName.substring(0, dotPosition);
		}

		return Optional.ofNullable(retryableConsumerMap.get(retryableTopicName));
	}

	private RetryableConsumerUtils()
	{
		// empty
	}
}
