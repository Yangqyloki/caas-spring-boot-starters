package com.hybris.caas.kafka.error;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.hybris.caas.kafka.util.CaasKafkaConstants.DEAD_LETTER_TOPIC_SUFFIX;
import static org.springframework.kafka.support.KafkaHeaders.DLT_EXCEPTION_FQCN;
import static org.springframework.kafka.support.KafkaHeaders.DLT_EXCEPTION_MESSAGE;
import static org.springframework.kafka.support.KafkaHeaders.DLT_EXCEPTION_STACKTRACE;
import static org.springframework.kafka.support.KafkaHeaders.DLT_ORIGINAL_OFFSET;
import static org.springframework.kafka.support.KafkaHeaders.DLT_ORIGINAL_PARTITION;
import static org.springframework.kafka.support.KafkaHeaders.DLT_ORIGINAL_TIMESTAMP;
import static org.springframework.kafka.support.KafkaHeaders.DLT_ORIGINAL_TIMESTAMP_TYPE;
import static org.springframework.kafka.support.KafkaHeaders.DLT_ORIGINAL_TOPIC;

/**
 * Extends {@link DeadLetterPublishingRecoverer} in order to manage the headers of the messages published to the retry topics or dead letter topic.
 * In the case when the message gets published to a retry topic, the message timestamp gets overridden with the current time (new publishing time).
 * In all cases the original topic, partition, and timestamp are preserved using DLT headers (see spring-kafka documentation on how DLT headers are named).
 * In addition to the topic information, the exception message is also added as DLT header to messages published to retry topics.
 * Also, in the case of dead letter topic all the DTL headers added by spring-kafka are kept to the published message.
 */
public class RetryableConsumerDeadLetterPublishingRecoverer extends DeadLetterPublishingRecoverer
{
	private static final Logger LOG = LoggerFactory.getLogger(RetryableConsumerDeadLetterPublishingRecoverer.class);
	private static final String[] ALL_DLT_HEADERS = { DLT_ORIGINAL_TOPIC, DLT_ORIGINAL_PARTITION, DLT_ORIGINAL_OFFSET,
			DLT_ORIGINAL_TIMESTAMP, DLT_ORIGINAL_TIMESTAMP_TYPE, DLT_EXCEPTION_FQCN, DLT_EXCEPTION_MESSAGE, DLT_EXCEPTION_STACKTRACE };
	private static final String[] MANAGED_DLT_HEADERS = { DLT_ORIGINAL_TOPIC, DLT_ORIGINAL_PARTITION, DLT_ORIGINAL_TIMESTAMP,
			DLT_ORIGINAL_TIMESTAMP_TYPE, DLT_EXCEPTION_MESSAGE };

	public RetryableConsumerDeadLetterPublishingRecoverer(final KafkaOperations<?, ?> template,
			final BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver)
	{
		super(template, destinationResolver);
	}

	private RecordHeaders updateHeaders(final String topicName, final Headers headers)
	{
		final RecordHeaders recordHeaders = new RecordHeaders(headers.toArray());

		manageDeadLetterHeaders(topicName, recordHeaders);

		return recordHeaders;
	}

	private void manageDeadLetterHeaders(final String topicName, final RecordHeaders recordHeaders)
	{
		final Optional<Header> topicHeader = getFirstHeader(recordHeaders, DLT_ORIGINAL_TOPIC);
		final Optional<Header> partitionHeader = getFirstHeader(recordHeaders, DLT_ORIGINAL_PARTITION);
		final Optional<Header> timestampHeader = getFirstHeader(recordHeaders, DLT_ORIGINAL_TIMESTAMP);
		final Optional<Header> timestampTypeHeader = getFirstHeader(recordHeaders, DLT_ORIGINAL_TIMESTAMP_TYPE);
		final Optional<Header> errorMessageHeader = Optional.ofNullable(recordHeaders.lastHeader(DLT_EXCEPTION_MESSAGE));

		final String[] headersToRemove = topicName.endsWith(DEAD_LETTER_TOPIC_SUFFIX) ? MANAGED_DLT_HEADERS : ALL_DLT_HEADERS;
		removeHeaders(recordHeaders, headersToRemove);

		topicHeader.ifPresent(recordHeaders::add);
		partitionHeader.ifPresent(recordHeaders::add);
		timestampHeader.ifPresent(recordHeaders::add);
		timestampTypeHeader.ifPresent(recordHeaders::add);
		errorMessageHeader.ifPresent(recordHeaders::add);
	}

	private void removeHeaders(final RecordHeaders recordHeaders, final String[] headersToRemove)
	{
		for (final String headerToRemove : headersToRemove)
		{
			recordHeaders.remove(headerToRemove);
		}
	}

	private Optional<Header> getFirstHeader(final RecordHeaders recordHeaders, final String key)
	{
		return Optional.ofNullable(recordHeaders.headers(key)).map(iterable -> {
			final Iterator<Header> headerIterator = iterable.iterator();

			if (headerIterator.hasNext())
			{
				return headerIterator.next();
			}

			return null;
		});
	}

	@Override
	protected void publish(final ProducerRecord<Object, Object> outRecord, final KafkaOperations<Object, Object> kafkaTemplate)
	{
		final long retryTimestamp = OffsetDateTime.now().toInstant().toEpochMilli();
		final RecordHeaders recordHeaders = updateHeaders(outRecord.topic(), outRecord.headers());

		final ProducerRecord<Object, Object> updatedOutRecord = new ProducerRecord<>(outRecord.topic(), outRecord.partition(),
				retryTimestamp, outRecord.key(), outRecord.value(), recordHeaders);

		final String updatedOutRecordTopicName = updatedOutRecord.topic();
		LOG.debug("Publishing message with timestamp {} to topic {}.", retryTimestamp, updatedOutRecordTopicName);

		super.publish(updatedOutRecord, kafkaTemplate);
	}
}
