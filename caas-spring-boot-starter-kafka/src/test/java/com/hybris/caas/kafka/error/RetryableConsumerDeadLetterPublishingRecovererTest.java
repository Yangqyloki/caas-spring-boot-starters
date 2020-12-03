package com.hybris.caas.kafka.error;

import com.google.common.base.Charsets;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.AsyncResult;

import java.time.Instant;
import java.util.UUID;

import static com.hybris.caas.kafka.util.CaasKafkaConstants.DEAD_LETTER_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.SHORT_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaHeaders.MESSAGE_ID;
import static com.hybris.caas.kafka.util.CaasKafkaHeaders.TENANT;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RetryableConsumerDeadLetterPublishingRecovererTest
{
	private static final String TOPIC_NAME = "dummy";
	private static final Integer PARTITION = 1;
	private static final String KEY = "key";
	private static final String VALUE = "value";
	private static final String ORIGINAL_TOPIC_HEADER = "kafka_dlt-original-topic";
	private static final String[] ESSENTIAL_HEADERS = { ORIGINAL_TOPIC_HEADER, "kafka_dlt-original-partition",
			"kafka_dlt-original-timestamp", "kafka_dlt-exception-message" };
	private static final String NON_ESSENTIAL_HEADER = "kafka_dlt-exception-fqcn";

	@Mock
	private KafkaTemplate<Object, Object> template;
	@Captor
	private ArgumentCaptor<ProducerRecord<Object, Object>> producerRecordArgumentCaptor;

	private RetryableConsumerDeadLetterPublishingRecoverer retryableConsumerDeadLetterPublishingRecoverer;

	@Before
	public void setUp()
	{
		retryableConsumerDeadLetterPublishingRecoverer = new RetryableConsumerDeadLetterPublishingRecoverer(template,
				((consumerRecord, e) -> new TopicPartition("dummy", 0)));

		when(template.send(ArgumentMatchers.<ProducerRecord<Object, Object>>any())).thenReturn(new AsyncResult<>(
				new SendResult<>(new ProducerRecord<>(TOPIC_NAME, KEY, VALUE), new RecordMetadata(null, 0, 0, 0, null, 0, 0))));
	}

	@Test
	public void should_set_current_time_as_timestamp_for_record()
	{
		final Headers recordHeaders = new RecordHeaders();
		recordHeaders.add(MESSAGE_ID, UUID.randomUUID().toString().getBytes(Charsets.UTF_8));
		recordHeaders.add(TENANT, UUID.randomUUID().toString().getBytes(Charsets.UTF_8));

		final Instant now = Instant.now();
		final ProducerRecord<Object, Object> outRecord = new ProducerRecord<>(TOPIC_NAME, PARTITION,
				now.minusSeconds(100).toEpochMilli(), KEY, VALUE, recordHeaders);

		retryableConsumerDeadLetterPublishingRecoverer.publish(outRecord, template);

		verify(template).send(producerRecordArgumentCaptor.capture());

		final ProducerRecord<Object, Object> publishedRecord = producerRecordArgumentCaptor.getValue();
		assertThat(publishedRecord.topic(), equalTo(outRecord.topic()));
		assertThat(publishedRecord.partition(), equalTo(outRecord.partition()));
		assertThat(publishedRecord.key(), equalTo(outRecord.key()));
		assertThat(publishedRecord.value(), equalTo(outRecord.value()));
		assertThat(publishedRecord.headers().toArray(), equalTo(outRecord.headers().toArray()));
		assertThat(publishedRecord.timestamp(), allOf(greaterThanOrEqualTo(now.toEpochMilli()), lessThan(now.toEpochMilli() + 100)));
	}

	@Test
	public void should_preserve_essential_dlt_headers_for_retry_topic()
	{
		testDltHeadersManagement(false);
	}

	@Test
	public void should_preserve_dlt_headers_for_dlt_topic()
	{
		testDltHeadersManagement(true);
	}

	private void testDltHeadersManagement(final boolean targetDltTopic)
	{
		final RecordHeaders recordHeaders = new RecordHeaders();
		for (String headerKey : ESSENTIAL_HEADERS)
		{
			recordHeaders.add(headerKey, VALUE.getBytes());
		}

		// add additional headers that need to be preserved
		recordHeaders.add(MESSAGE_ID, UUID.randomUUID().toString().getBytes(Charsets.UTF_8));
		recordHeaders.add(TENANT, UUID.randomUUID().toString().getBytes(Charsets.UTF_8));

		recordHeaders.add(NON_ESSENTIAL_HEADER, VALUE.getBytes()).add(ORIGINAL_TOPIC_HEADER, TOPIC_NAME.getBytes());

		final ProducerRecord<Object, Object> outRecord = new ProducerRecord<>(
				TOPIC_NAME + (targetDltTopic ? DEAD_LETTER_TOPIC_SUFFIX : SHORT_DELAY_RETRY_TOPIC_SUFFIX), PARTITION,
				Instant.now().toEpochMilli(), KEY, VALUE, recordHeaders);

		retryableConsumerDeadLetterPublishingRecoverer.publish(outRecord, template);

		verify(template).send(producerRecordArgumentCaptor.capture());

		if (!targetDltTopic)
		{
			recordHeaders.remove(NON_ESSENTIAL_HEADER);
		}

		// restore original topic header (single occurrence)
		recordHeaders.remove(ORIGINAL_TOPIC_HEADER).add(ORIGINAL_TOPIC_HEADER, VALUE.getBytes());

		final ProducerRecord<Object, Object> publishedRecord = producerRecordArgumentCaptor.getValue();
		assertThat(publishedRecord.headers().toArray().length, equalTo(recordHeaders.toArray().length));

		for (String headerKey : ESSENTIAL_HEADERS)
		{
			assertThat(publishedRecord.headers().lastHeader(headerKey), equalTo(recordHeaders.lastHeader(headerKey)));
		}

		assertThat(publishedRecord.headers().lastHeader(MESSAGE_ID), equalTo(recordHeaders.lastHeader(MESSAGE_ID)));
		assertThat(publishedRecord.headers().lastHeader(TENANT), equalTo(recordHeaders.lastHeader(TENANT)));

		if (targetDltTopic)
		{
			assertThat(publishedRecord.headers().lastHeader(NON_ESSENTIAL_HEADER),
					equalTo(recordHeaders.lastHeader(NON_ESSENTIAL_HEADER)));
		}
	}
}
