package com.hybris.caas.kafka.error;

import com.hybris.caas.kafka.config.CaasKafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.hybris.caas.kafka.util.CaasKafkaConstants.DEAD_LETTER_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.LONG_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.SHORT_DELAY_RETRY_TOPIC_SUFFIX;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class RetryableConsumerDestinationTopicResolverTest
{
	private static final String DUMMY_TOPIC_NAME = "dummy";

	private Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap = new HashMap<>();
	private Exception ex = new Exception();
	private ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>(DUMMY_TOPIC_NAME, 10, 15, null, null);
	private RetryableConsumerDestinationTopicResolver retryableConsumerDestinationTopicResolver;

	@Before
	public void setUp()
	{
		final CaasKafkaProperties.RetryableConsumer retryableConsumer = new CaasKafkaProperties.RetryableConsumer();
		retryableConsumer.setSource(DUMMY_TOPIC_NAME);

		final CaasKafkaProperties.RetryTopic shortDelayRetryTopic = new CaasKafkaProperties.RetryTopic();
		shortDelayRetryTopic.setMessageDelayMs(2);

		final CaasKafkaProperties.RetryTopic longDelayRetryTopic = new CaasKafkaProperties.RetryTopic();
		longDelayRetryTopic.setMessageDelayMs(5);

		final CaasKafkaProperties.DeadLetterTopic deadLetterTopic = new CaasKafkaProperties.DeadLetterTopic();

		retryableConsumer.setShortDelayRetry(shortDelayRetryTopic);
		retryableConsumer.setLongDelayRetry(longDelayRetryTopic);
		retryableConsumer.setDeadLetter(deadLetterTopic);

		retryableConsumerMap.put(DUMMY_TOPIC_NAME, retryableConsumer);

		retryableConsumerDestinationTopicResolver = new RetryableConsumerDestinationTopicResolver(retryableConsumerMap);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void should_throw_UnsupportedOperationException_for_DLT_record()
	{
		final ConsumerRecord<?, ?> record = new ConsumerRecord<>(DUMMY_TOPIC_NAME + DEAD_LETTER_TOPIC_SUFFIX, 10, 15, null, null);

		retryableConsumerDestinationTopicResolver.getTopicPartition(record, ex);
	}

	@Test(expected = IllegalStateException.class)
	public void should_throw_IllegalStateException_non_retry_consumer()
	{
		final ConsumerRecord<?, ?> record = new ConsumerRecord<>(DUMMY_TOPIC_NAME + "abc", 10, 15, null, null);

		retryableConsumerDestinationTopicResolver.getTopicPartition(record, ex);
	}

	@Test(expected = KafkaRuntimeException.class)
	public void should_throw_KafkaRuntimeException_for_LDR_record_when_no_DLT_config()
	{
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setDeadLetter(null);
		final ConsumerRecord<?, ?> record = new ConsumerRecord<>(DUMMY_TOPIC_NAME + LONG_DELAY_RETRY_TOPIC_SUFFIX, 10, 15, null, null);

		retryableConsumerDestinationTopicResolver.getTopicPartition(record, ex);
	}

	@Test
	public void should_return_DLT_topic_for_LDR()
	{
		final ConsumerRecord<?, ?> record = new ConsumerRecord<>(DUMMY_TOPIC_NAME + LONG_DELAY_RETRY_TOPIC_SUFFIX, 10, 15, null, null);

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(record, ex);

		assertTrue(result.topic().endsWith(DEAD_LETTER_TOPIC_SUFFIX));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test
	public void should_return_DLT_topic_for_LDR_with_topic_prefix_configured()
	{
		final ConsumerRecord<?, ?> record = new ConsumerRecord<>(DUMMY_TOPIC_NAME + LONG_DELAY_RETRY_TOPIC_SUFFIX, 10, 15, null, null);
		record.headers().add(KafkaHeaders.DLT_ORIGINAL_TOPIC, DUMMY_TOPIC_NAME.getBytes(StandardCharsets.UTF_8));

		retryableConsumerMap.get(DUMMY_TOPIC_NAME).getDeadLetter().setTopicPrefix("dltTopicPrefix");

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(record, ex);

		assertThat(result.topic(), equalTo("dltTopicPrefix.DLT"));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test
	public void should_return_LDR_topic_for_SDR()
	{
		final ConsumerRecord<?, ?> record = new ConsumerRecord<>(DUMMY_TOPIC_NAME + SHORT_DELAY_RETRY_TOPIC_SUFFIX, 10, 15, null,
				null);

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(record, ex);

		assertTrue(result.topic().endsWith(LONG_DELAY_RETRY_TOPIC_SUFFIX));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test
	public void should_return_LDR_topic_for_SDR_with_topic_prefix_configured()
	{
		final ConsumerRecord<?, ?> record = new ConsumerRecord<>(DUMMY_TOPIC_NAME + SHORT_DELAY_RETRY_TOPIC_SUFFIX, 10, 15, null,
				null);
		record.headers().add(KafkaHeaders.DLT_ORIGINAL_TOPIC, DUMMY_TOPIC_NAME.getBytes(StandardCharsets.UTF_8));

		retryableConsumerMap.get(DUMMY_TOPIC_NAME).getLongDelayRetry().setTopicPrefix("ldrTopicPrefix");

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(record, ex);

		assertThat(result.topic(), equalTo("ldrTopicPrefix.LDR"));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test
	public void should_return_DLT_topic_for_SDR_when_no_LDR_config()
	{
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setLongDelayRetry(null);
		final ConsumerRecord<?, ?> record = new ConsumerRecord<>(DUMMY_TOPIC_NAME + SHORT_DELAY_RETRY_TOPIC_SUFFIX, 10, 15, null,
				null);

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(record, ex);

		assertTrue(result.topic().endsWith(DEAD_LETTER_TOPIC_SUFFIX));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test
	public void should_return_DLT_topic_for_SDR_when_no_LDR_config_with_topic_prefix_configured()
	{
		final ConsumerRecord<?, ?> record = new ConsumerRecord<>(DUMMY_TOPIC_NAME + SHORT_DELAY_RETRY_TOPIC_SUFFIX, 10, 15, null,
				null);
		record.headers().add(KafkaHeaders.DLT_ORIGINAL_TOPIC, DUMMY_TOPIC_NAME.getBytes(StandardCharsets.UTF_8));

		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setLongDelayRetry(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).getDeadLetter().setTopicPrefix("dltTopicPrefix");

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(record, ex);

		assertThat(result.topic(), equalTo("dltTopicPrefix.DLT"));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test(expected = KafkaRuntimeException.class)
	public void should_throw_KafkaRuntimeException_for_SDR_record_when_no_LDR_and_no_DLT_config()
	{
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setLongDelayRetry(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setDeadLetter(null);
		final ConsumerRecord<?, ?> record = new ConsumerRecord<>(DUMMY_TOPIC_NAME + SHORT_DELAY_RETRY_TOPIC_SUFFIX, 10, 15, null,
				null);

		retryableConsumerDestinationTopicResolver.getTopicPartition(record, ex);
	}

	@Test
	public void should_return_SDR_topic_for_regular_record()
	{
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setLongDelayRetry(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setDeadLetter(null);

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(consumerRecord, ex);

		assertTrue(result.topic().endsWith(SHORT_DELAY_RETRY_TOPIC_SUFFIX));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test
	public void should_return_SDR_topic_for_regular_record_with_topic_prefix_configured()
	{
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setLongDelayRetry(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setDeadLetter(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).getShortDelayRetry().setTopicPrefix("sdrTopicPrefix");

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(consumerRecord, ex);

		assertThat(result.topic(), equalTo("sdrTopicPrefix.SDR"));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test
	public void should_return_LDR_topic_for_regular_record()
	{
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setShortDelayRetry(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setDeadLetter(null);

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(consumerRecord, ex);

		assertTrue(result.topic().endsWith(LONG_DELAY_RETRY_TOPIC_SUFFIX));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test
	public void should_return_LDR_topic_for_regular_record_with_topic_prefix_configured()
	{
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setShortDelayRetry(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setDeadLetter(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).getLongDelayRetry().setTopicPrefix("ldrTopicPrefix");

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(consumerRecord, ex);

		assertThat(result.topic(), equalTo("ldrTopicPrefix.LDR"));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test
	public void should_return_DLT_topic_for_regular_record()
	{
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setShortDelayRetry(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setLongDelayRetry(null);

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(consumerRecord, ex);

		assertTrue(result.topic().endsWith(DEAD_LETTER_TOPIC_SUFFIX));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test
	public void should_return_DLT_topic_for_regular_record_with_topic_prefix_configured()
	{
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setShortDelayRetry(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setLongDelayRetry(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).getDeadLetter().setTopicPrefix("dltTopicPrefix");

		final TopicPartition result = retryableConsumerDestinationTopicResolver.getTopicPartition(consumerRecord, ex);

		assertThat(result.topic(), equalTo("dltTopicPrefix.DLT"));
		assertThat(result.partition(), equalTo(-1));
	}

	@Test(expected = KafkaRuntimeException.class)
	public void should_throw_KafkaRuntimeException_for_regular_record_when_no_SDR_and_no_LDR_and_no_DLT_config()
	{
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setShortDelayRetry(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setLongDelayRetry(null);
		retryableConsumerMap.get(DUMMY_TOPIC_NAME).setDeadLetter(null);

		retryableConsumerDestinationTopicResolver.getTopicPartition(consumerRecord, ex);
	}
}
