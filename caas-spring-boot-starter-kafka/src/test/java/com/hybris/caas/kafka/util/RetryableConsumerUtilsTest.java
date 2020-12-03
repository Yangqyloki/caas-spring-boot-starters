package com.hybris.caas.kafka.util;

import com.hybris.caas.kafka.config.CaasKafkaProperties;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.hybris.caas.kafka.util.CaasKafkaConstants.DEAD_LETTER_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.LONG_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.SHORT_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.RetryableConsumerUtils.getRetryableConsumerByTopicName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RetryableConsumerUtilsTest
{
	private final CaasKafkaProperties props = new CaasKafkaProperties();

	private static final String PRODUCER_1 = "producer1";
	private static final String PRODUCER_2 = "producer2";
	private static final String PRODUCER_3 = "producer3";

	private static final String NEW_PREFIX = "new";

	private static final String DUMMY_TOPIC_NAME = "dummy";

	private final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap = Collections.singletonMap(DUMMY_TOPIC_NAME,
			new CaasKafkaProperties.RetryableConsumer());

	private final CaasKafkaProperties.Producer newProducer1 = createProducer(PRODUCER_1, NEW_PREFIX);
	private final CaasKafkaProperties.Producer newProducer2 = createProducer(PRODUCER_2, NEW_PREFIX);
	private final CaasKafkaProperties.Producer newProducer3 = createProducer(PRODUCER_3, NEW_PREFIX);

	@Before
	@SuppressWarnings("squid:CallToDeprecatedMethod")
	public void setUp()
	{
		final Map<String, CaasKafkaProperties.Producer> producerMap = props.getProducer();

		producerMap.put(PRODUCER_1, newProducer1);
		producerMap.put(PRODUCER_2, newProducer2);
		producerMap.put(PRODUCER_3, newProducer3);
	}

	private CaasKafkaProperties.Producer createProducer(final String bindingName, final String prefix)
	{
		final CaasKafkaProperties.Producer producer = new CaasKafkaProperties.Producer();
		producer.setDestination(prefix + bindingName);
		return producer;
	}

	@Test
	public void should_replace_expected_suffix()
	{
		final String result = RetryableConsumerUtils.replaceTopicSuffix("dummy-retry", "-retry", "-dlt");
		assertThat(result, equalTo("dummy-dlt"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_IllegalArgumentException_when_expected_suffix_is_not_present()
	{
		RetryableConsumerUtils.replaceTopicSuffix("dummy-retr", "-retry", "-dlt");
	}

	@Test
	public void should_not_find_retryable_consumer()
	{
		final Optional<CaasKafkaProperties.RetryableConsumer> result = getRetryableConsumerByTopicName(Collections.emptyMap(),
				DUMMY_TOPIC_NAME);
		assertFalse(result.isPresent());
	}

	@Test
	public void should_find_retryable_consumer_by_regular_topic_name()
	{
		final Optional<CaasKafkaProperties.RetryableConsumer> result = getRetryableConsumerByTopicName(retryableConsumerMap,
				DUMMY_TOPIC_NAME);
		assertTrue(result.isPresent());
	}

	@Test
	public void should_find_retryable_consumer_by_short_retry_topic_name()
	{
		final Optional<CaasKafkaProperties.RetryableConsumer> result = getRetryableConsumerByTopicName(retryableConsumerMap,
				DUMMY_TOPIC_NAME + SHORT_DELAY_RETRY_TOPIC_SUFFIX);
		assertTrue(result.isPresent());
	}

	@Test
	public void should_find_retryable_consumer_by_long_retry_topic_name()
	{
		final Optional<CaasKafkaProperties.RetryableConsumer> result = getRetryableConsumerByTopicName(retryableConsumerMap,
				DUMMY_TOPIC_NAME + LONG_DELAY_RETRY_TOPIC_SUFFIX);
		assertTrue(result.isPresent());
	}

	@Test
	public void should_find_retryable_consumer_by_dlt_topic_name()
	{
		final Optional<CaasKafkaProperties.RetryableConsumer> result = getRetryableConsumerByTopicName(retryableConsumerMap,
				DUMMY_TOPIC_NAME + DEAD_LETTER_TOPIC_SUFFIX);
		assertTrue(result.isPresent());
	}
}
