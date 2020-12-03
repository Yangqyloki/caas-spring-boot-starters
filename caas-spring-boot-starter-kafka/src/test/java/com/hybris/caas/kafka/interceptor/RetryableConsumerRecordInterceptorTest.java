package com.hybris.caas.kafka.interceptor;

import com.hybris.caas.kafka.config.CaasKafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.record.TimestampType;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.hybris.caas.kafka.util.CaasKafkaConstants.LONG_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.SHORT_DELAY_RETRY_TOPIC_SUFFIX;
import static org.apache.kafka.clients.consumer.ConsumerRecord.NULL_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RetryableConsumerRecordInterceptorTest
{
	private static final String DUMMY_TOPIC_NAME = "dummy";
	private static final int MAX_VARIANCE_MS = 100;
	private static final int EXECUTION_VARIANCE_MS = 50;
	private static final int SHORT_DELAY_MS = 200;
	private static final int LONG_DELAY_MS = 500;
	private static final long IDLE_BETWEEN_RETRY_POLLS_MS = 1_000;

	private Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap = new HashMap<>();
	private RetryableConsumerRecordInterceptor<Object, Object> interceptor;

	@Before
	public void setUp()
	{
		final CaasKafkaProperties.RetryableConsumer retryableConsumer = new CaasKafkaProperties.RetryableConsumer();
		retryableConsumer.setSource(DUMMY_TOPIC_NAME);

		final CaasKafkaProperties.RetryTopic shortDelayRetryTopic = new CaasKafkaProperties.RetryTopic();
		shortDelayRetryTopic.setMessageDelayMs(SHORT_DELAY_MS);

		final CaasKafkaProperties.RetryTopic longDelayRetryTopic = new CaasKafkaProperties.RetryTopic();
		longDelayRetryTopic.setMessageDelayMs(LONG_DELAY_MS);

		final CaasKafkaProperties.DeadLetterTopic deadLetterTopic = new CaasKafkaProperties.DeadLetterTopic();

		retryableConsumer.setShortDelayRetry(shortDelayRetryTopic);
		retryableConsumer.setLongDelayRetry(longDelayRetryTopic);
		retryableConsumer.setDeadLetter(deadLetterTopic);

		retryableConsumerMap.put(DUMMY_TOPIC_NAME, retryableConsumer);

		interceptor = new RetryableConsumerRecordInterceptor<>(retryableConsumerMap, IDLE_BETWEEN_RETRY_POLLS_MS);
	}

	@Test
	public void should_not_delay_processing_of_record_not_from_retry_topic()
	{
		final ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(DUMMY_TOPIC_NAME, 0, 0, null, null);

		assertRecordInterceptionTimeElapsedBetween(consumerRecord, 0, MAX_VARIANCE_MS);
	}

	@Test
	public void should_not_delay_processing_of_record_not_handled_by_retryable_consumer_topic()
	{
		final ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>("test-1", 0, 0, null, null);

		assertRecordInterceptionTimeElapsedBetween(consumerRecord, 0, MAX_VARIANCE_MS);
	}

	@Test
	public void should_not_delay_processing_of_record_with_expired_delay()
	{
		final ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(DUMMY_TOPIC_NAME + SHORT_DELAY_RETRY_TOPIC_SUFFIX,
				0, 0, Instant.now().minusSeconds(10).toEpochMilli(), TimestampType.CREATE_TIME, ConsumerRecord.NULL_CHECKSUM,
				NULL_SIZE, NULL_SIZE, null, null);

		assertRecordInterceptionTimeElapsedBetween(consumerRecord, 0, MAX_VARIANCE_MS);
	}

	@Test
	public void should_delay_processing_of_record_from_short_delay_retry_topic()
	{
		final ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(DUMMY_TOPIC_NAME + SHORT_DELAY_RETRY_TOPIC_SUFFIX,
				0, 0, Instant.now().toEpochMilli(), TimestampType.CREATE_TIME, ConsumerRecord.NULL_CHECKSUM, NULL_SIZE, NULL_SIZE,
				null, null);

		assertRecordInterceptionTimeElapsedBetween(consumerRecord, SHORT_DELAY_MS - EXECUTION_VARIANCE_MS,
				SHORT_DELAY_MS + MAX_VARIANCE_MS);
	}

	@Test
	public void should_delay_processing_of_record_from_long_delay_retry_topic()
	{
		final ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(DUMMY_TOPIC_NAME + LONG_DELAY_RETRY_TOPIC_SUFFIX, 0,
				0, Instant.now().toEpochMilli(), TimestampType.CREATE_TIME, ConsumerRecord.NULL_CHECKSUM, NULL_SIZE, NULL_SIZE, null,
				null);

		assertRecordInterceptionTimeElapsedBetween(consumerRecord, LONG_DELAY_MS - EXECUTION_VARIANCE_MS,
				LONG_DELAY_MS + MAX_VARIANCE_MS);
	}

	@Test
	public void should_throw_DelayException_when_large_delay_is_required_after_min_sleep()
	{
		final ConsumerRecord<Object, Object> consumerRecord = new ConsumerRecord<>(DUMMY_TOPIC_NAME + LONG_DELAY_RETRY_TOPIC_SUFFIX, 0,
				0, Instant.now().plusMillis(IDLE_BETWEEN_RETRY_POLLS_MS).toEpochMilli(), TimestampType.CREATE_TIME,
				ConsumerRecord.NULL_CHECKSUM, NULL_SIZE, NULL_SIZE, null, null);

		final Instant beforeInterceptorTimestamp = Instant.now();

		try
		{
			interceptor.intercept(consumerRecord);
			fail();
		}
		catch (final DelayException ex)
		{
			final Instant afterInterceptorTiemstamp = Instant.now();
			final long timeElapsed = Duration.between(beforeInterceptorTimestamp, afterInterceptorTiemstamp).toMillis();
			assertTrue("Time elapsed not greater than minimum interval: " + timeElapsed,
					timeElapsed >= IDLE_BETWEEN_RETRY_POLLS_MS - EXECUTION_VARIANCE_MS);
			assertTrue("Time elapsed not smaller than maximum interval: " + timeElapsed,
					timeElapsed <= IDLE_BETWEEN_RETRY_POLLS_MS + MAX_VARIANCE_MS);
		}
		catch (final Exception ex)
		{
			fail();
		}
	}

	private void assertRecordInterceptionTimeElapsedBetween(final ConsumerRecord<Object, Object> consumerRecord,
			final long minIntervalMs, final long maxIntervalMs)
	{
		final Instant beforeInterceptorTimestamp = Instant.now();
		final ConsumerRecord<Object, Object> result = interceptor.intercept(consumerRecord);
		final Instant afterInterceptorTiemstamp = Instant.now();

		final long timeElapsed = Duration.between(beforeInterceptorTimestamp, afterInterceptorTiemstamp).toMillis();
		assertTrue("Time elapsed not greater than minimum interval: " + timeElapsed, timeElapsed >= minIntervalMs);
		assertTrue("Time elapsed not smaller than maximum interval: " + timeElapsed, timeElapsed <= maxIntervalMs);
		assertEquals("Consumer record was changed", consumerRecord, result);
	}
}
