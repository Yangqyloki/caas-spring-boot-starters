package com.hybris.caas.kafka.util;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class ConsumerRecordUtilsTest
{
	private static final String TEST_HEADER = "test-header";
	private static final String HEADER_VALUE_1 = "header-value-1";
	private static final String HEADER_VALUE_2 = "header-value-2";
	private static final String KEY = "key";
	private static final String VALUE = "value";

	@Test
	public void should_get_last_header_value()
	{
		final var consumerRecord = buildConsumerRecord();

		Assertions.assertEquals(HEADER_VALUE_2, ConsumerRecordUtils.getLastConsumerRecordHeader(consumerRecord, TEST_HEADER));
		Assertions.assertNull(ConsumerRecordUtils.getLastConsumerRecordHeader(consumerRecord, "dummy-header-name"));
	}

	private ConsumerRecord<?, ?> buildConsumerRecord()
	{
		final Headers headers = new RecordHeaders();
		headers.add(TEST_HEADER, HEADER_VALUE_1.getBytes(StandardCharsets.UTF_8));
		headers.add(TEST_HEADER, HEADER_VALUE_2.getBytes(StandardCharsets.UTF_8));

		return new ConsumerRecord("topic", 0, 0, Instant.now().getEpochSecond(), TimestampType.CREATE_TIME, null, KEY.length(),
				VALUE.length(), KEY, VALUE, headers);
	}
}
