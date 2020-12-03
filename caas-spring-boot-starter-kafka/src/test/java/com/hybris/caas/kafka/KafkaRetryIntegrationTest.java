package com.hybris.caas.kafka;

import com.google.common.collect.ImmutableMap;
import com.hybris.caas.kafka.config.CaasKafkaConfig;
import com.hybris.caas.kafka.config.CaasKafkaTransactionConfig;
import com.hybris.caas.kafka.message.MessageAssembler;
import com.hybris.caas.kafka.message.MessageAssemblerImpl;
import com.hybris.caas.kafka.util.CaasKafkaHeaders;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteConsumerGroupsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.assertj.core.api.AbstractMapAssert;
import org.assertj.core.api.MapAssert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * See {@code src/test/resources/application.yml}, profiles {@code kafka-retry-*} for spring kafka configuration.
 */
@RunWith(Enclosed.class)
public class KafkaRetryIntegrationTest
{
	private static final Logger log = LoggerFactory.getLogger(KafkaRetryIntegrationTest.class);
	public static final int DEFAULT_TIMEOUT_IN_SEC = 5;

	@RunWith(SpringRunner.class)
	@SpringBootTest(classes = Application.class, properties = { "tenant.multiTenantSessionProperty=eclipselink.tenant-id",
			"spring.kafka.listener.missing-topics-fatal=false" })
	@ContextConfiguration(classes = { BaseKafkaRetry.RetryConfig.class, CaasKafkaConfig.class, CaasKafkaTransactionConfig.class,
			BaseKafkaRetry.Config.class })
	@DirtiesContext
	public static abstract class BaseKafkaRetry
	{
		@Autowired
		protected AdminClient adminClient;
		@Autowired
		protected KafkaTemplate kafkaTemplate;
		@Autowired
		protected KafkaMessageReceiver kafkaMessageReceiver;
		@Value("${caas.kafka.test}")
		protected String test;

		protected MessageAssembler<Map<String, String>> messageAssembler;

		protected abstract Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap();

		@PostConstruct
		protected void buildMessageAssembler()
		{
			this.messageAssembler = new MessageAssemblerImpl<>(test + "-topic");
			this.kafkaMessageReceiver.setConsumers(consumerPerTopicMap());
		}

		protected Message<Map<String, String>> buildMessage()
		{
			return messageAssembler.assemble(UUID.randomUUID().toString(), "Gandalf-" + test,
					ImmutableMap.of("quote", "He that breaks a thing to find out what it is has left the path of wisdom." + test));
		}

		@Configuration
		public static class Config
		{
			private AdminClient adminClient;

			@Value("${spring.kafka.consumer.group-id}")
			private String groupId;

			@Bean
			public AdminClient adminClient(KafkaAdmin kafkaAdmin)
			{
				this.adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
				return adminClient;
			}

			@Bean
			public KafkaMessageReceiver kafkaMessageReceiver()
			{
				return new KafkaMessageReceiver();
			}

			@EventListener
			public void handleContextClosedEvent(ContextClosedEvent contextClosedEvent)
			{
				try
				{
					final ListTopicsResult listTopicsResult = adminClient.listTopics();
					final Set<String> topics = listTopicsResult.names()
							.get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
							.stream()
							.filter(name -> name.startsWith("tdr-"))
							.collect(Collectors.toSet());

					log.info("Deleting consumer group {}", groupId);
					final DeleteConsumerGroupsResult deleteConsumerGroupsResult = adminClient.deleteConsumerGroups(Set.of(groupId));
					deleteConsumerGroupsResult.deletedGroups().get(groupId).get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

					log.info("Deleting topics {}", topics);
					final DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(topics);
					deleteTopicsResult.all().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);
				}
				catch (InterruptedException | TimeoutException | ExecutionException e)
				{
					log.error(
							"Failed to delete tdr- prefixed topics, check kafka broker is up and running (sometimes kafka broker fails to start)",
							e);
				}
			}
		}

		@Configuration
		public static class RetryConfig
		{
			@Bean
			@Profile("kafka-retry-short-long-dlt-with-stateless-retry-template-and-exceptions-map")
			public Map<Class<? extends Throwable>, Boolean> kafkaListenerRetryExceptionsMap()
			{
				return Collections.singletonMap(IllegalArgumentException.class, false);
			}
		}

		public static class KafkaMessageReceiver
		{
			private final Map<String, Deque<Message<Map<String, String>>>> collector = new ConcurrentHashMap<>();
			private Map<String, Consumer<Message<Map<String, String>>>> consumers;

			// unable to inject at constructor due to inheritance and springboot lifecycle,
			// passing map via @PostConstruct to have access to abstract methods impl in child class
			public void setConsumers(final Map<String, Consumer<Message<Map<String, String>>>> consumers)
			{
				this.consumers = consumers;
			}

			@KafkaListener(topicPattern = "(${caas.kafka.test}-topic.*|${caas.kafka.test}-prefix-.*)", containerFactory = "retryableKafkaListenerContainerFactory")
			public void failingMessage(@Payload final Message<Map<String, String>> message,
					@Header(KafkaHeaders.RECEIVED_TOPIC) final String topic)
			{
				log.info("Received in topic {}, the message {}", topic, message);
				collector.computeIfAbsent(topic, key -> new ConcurrentLinkedDeque<>()).offer(message);
				consumers.get(topic).accept(message);
			}
		}
	}

	@ActiveProfiles({ "base-config-kafka-retry", "kafka-retry-short-long-dlt" })
	public static class KafkaRetryShortLongDlt extends BaseKafkaRetry
	{
		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder() //
					.put(test + "-topic", message -> throwIllegalArgumentException())
					.put(test + "-topic.SDR", message -> throwIllegalArgumentException())
					.put(test + "-topic.LDR", message -> throwIllegalArgumentException())
					.put(test + "-topic.DLT", message -> latch.countDown())
					.build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic", test + "-topic.SDR", test + "-topic.LDR", test + "-topic.DLT");
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.executeInTransaction(kt -> {
				return kt.send(message);
			});
			latch.await(5, TimeUnit.SECONDS);

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage) //
							.containsEntry(KafkaHeaders.DLT_ORIGINAL_TOPIC, (test + "-topic").getBytes(StandardCharsets.UTF_8)));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage) //
							.containsEntry(KafkaHeaders.DLT_ORIGINAL_TOPIC, (test + "-topic").getBytes(StandardCharsets.UTF_8)));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage) //
							.containsEntry(KafkaHeaders.DLT_ORIGINAL_TOPIC, (test + "-topic").getBytes(StandardCharsets.UTF_8)));
		}
	}

	@ActiveProfiles({ "base-config-kafka-retry", "kafka-retry-short-long-dlt-with-topic-prefix" })
	public static class KafkaRetryShortLongDltWithTopicPrefix extends BaseKafkaRetry
	{
		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder() //
					.put(test + "-topic", message -> throwIllegalArgumentException())
					.put(test + "-prefix-short.SDR", message -> throwIllegalArgumentException())
					.put(test + "-prefix-long.LDR", message -> throwIllegalArgumentException())
					.put(test + "-prefix-dead.DLT", message -> latch.countDown())
					.build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic", test + "-prefix-short.SDR", test + "-prefix-long.LDR",
					test + "-prefix-dead.DLT");
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-prefix-short.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-prefix-long.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-prefix-dead.DLT")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.executeInTransaction(kt -> {
				return kt.send(message);
			});
			latch.await(5, TimeUnit.SECONDS);

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			assertThat(kafkaMessageReceiver.collector.get(test + "-prefix-short.SDR")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage) //
							.containsEntry(KafkaHeaders.DLT_ORIGINAL_TOPIC, (test + "-topic").getBytes(StandardCharsets.UTF_8)));
			assertThat(kafkaMessageReceiver.collector.get(test + "-prefix-long.LDR")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage) //
							.containsEntry(KafkaHeaders.DLT_ORIGINAL_TOPIC, (test + "-topic").getBytes(StandardCharsets.UTF_8)));
			assertThat(kafkaMessageReceiver.collector.get(test + "-prefix-dead.DLT")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage) //
							.containsEntry(KafkaHeaders.DLT_ORIGINAL_TOPIC, (test + "-topic").getBytes(StandardCharsets.UTF_8)));
		}
	}

	@ActiveProfiles({ "base-config-kafka-retry", "kafka-retry-short-long-dlt",
							"kafka-retry-short-long-dlt-with-stateless-retry-template" })
	public static class KafkaRetryShortLongDltWithStatelessRetryTemplate extends BaseKafkaRetry
	{
		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder() //
					.put(test + "-topic", message -> throwIllegalArgumentException())
					.put(test + "-topic.SDR", message -> throwIllegalArgumentException())
					.put(test + "-topic.LDR", message -> throwIllegalArgumentException())
					.put(test + "-topic.DLT", message -> latch.countDown())
					.build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic", test + "-topic.SDR", test + "-topic.LDR", test + "-topic.DLT");
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.executeInTransaction(kt -> {
				return kt.send(message);
			});
			latch.await(5, TimeUnit.SECONDS);

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).allSatisfy(
					collectedMessage -> assertKafkaMessage(message, collectedMessage)).hasSize(2);
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).allSatisfy(
					collectedMessage -> assertKafkaMessage(message, collectedMessage)).hasSize(2);
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).allSatisfy(
					collectedMessage -> assertKafkaMessage(message, collectedMessage)).hasSize(2);
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
		}
	}

	@ActiveProfiles({ "base-config-kafka-retry", "kafka-retry-short-long-dlt",
							"kafka-retry-short-long-dlt-with-stateless-retry-template-and-exceptions-map" })
	public static class KafkaRetryShortLongDltWithStatelessRetryTemplateAndExceptionsMap extends BaseKafkaRetry
	{
		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder() //
					.put(test + "-topic", message -> throwIllegalArgumentException())
					.put(test + "-topic.SDR", message -> throwIllegalArgumentException())
					.put(test + "-topic.LDR", message -> throwIllegalArgumentException())
					.put(test + "-topic.DLT", message -> latch.countDown())
					.build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic", test + "-topic.SDR", test + "-topic.LDR", test + "-topic.DLT");
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.executeInTransaction(kt -> {
				return kt.send(message);
			});
			latch.await(5, TimeUnit.SECONDS);

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
		}
	}

	@ActiveProfiles({ "base-config-kafka-retry", "kafka-retry-short-dlt" })
	public static class KafkaRetryShortDlt extends BaseKafkaRetry
	{
		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder() //
					.put(test + "-topic", message -> throwIllegalArgumentException())
					.put(test + "-topic.SDR", message -> throwIllegalArgumentException())
					.put(test + "-topic.DLT", message -> latch.countDown())
					.build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic", test + "-topic.SDR", test + "-topic.DLT").doesNotContain(test + "-topic.LDR");
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.executeInTransaction(kt -> {
				return kt.send(message);
			});
			latch.await(5, TimeUnit.SECONDS);

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
		}
	}

	@ActiveProfiles({ "base-config-kafka-retry", "kafka-retry-long-dlt" })
	public static class KafkaRetryLongDlt extends BaseKafkaRetry
	{
		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder() //
					.put(test + "-topic", message -> throwIllegalArgumentException())
					.put(test + "-topic.LDR", message -> throwIllegalArgumentException())
					.put(test + "-topic.DLT", message -> latch.countDown())
					.build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic", test + "-topic.LDR", test + "-topic.DLT").doesNotContain(test + "-topic.SDR");
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.executeInTransaction(kt -> {
				return kt.send(message);
			});
			latch.await(5, TimeUnit.SECONDS);

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
		}
	}

	@ActiveProfiles({ "base-config-kafka-retry", "kafka-retry-short" })
	public static class KafkaRetryShort extends BaseKafkaRetry
	{
		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder() //
					.put(test + "-topic", message -> throwIllegalArgumentException())
					.put(test + "-topic.SDR", message -> throwIllegalArgumentException())
					.build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic", test + "-topic.SDR").doesNotContain(test + "-topic.LDR", test + "-topic.DLT");
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.executeInTransaction(kt -> {
				return kt.send(message);
			});
			latch.await(5, TimeUnit.SECONDS); // this will timeout and continue, no latch count down called in this case

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			// as no DLT topic configured, the message is retried `caas.kafka.retryable-consumer.max-attempts` (configured as 2 for this tests)
			// so we have the original message received, plus 2 more attempts to process the message
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).hasSize(3);
			assertKafkaMessage(message, kafkaMessageReceiver.collector.get(test + "-topic.SDR").poll());
			assertKafkaMessage(message, kafkaMessageReceiver.collector.get(test + "-topic.SDR").poll());
			assertKafkaMessage(message, kafkaMessageReceiver.collector.get(test + "-topic.SDR").poll());
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isEmpty();

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();
		}
	}

	@ActiveProfiles({ "base-config-kafka-retry", "kafka-retry-long" })
	public static class KafkaRetryLong extends BaseKafkaRetry
	{
		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder() //
					.put(test + "-topic", message -> throwIllegalArgumentException())
					.put(test + "-topic.LDR", message -> throwIllegalArgumentException())
					.build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic", test + "-topic.LDR").doesNotContain(test + "-topic.SDR", test + "-topic.DLT");
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.executeInTransaction(kt -> {
				return kt.send(message);
			});
			latch.await(5, TimeUnit.SECONDS); // this will timeout and continue, no latch count down called in this case

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			// as no DLT topic configured, the message is retried `caas.kafka.retryable-consumer.max-attempts` (configured as 2 for this tests)
			// so we have the original message received, plus 2 more attempts to process the message
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).hasSize(3);
			assertKafkaMessage(message, kafkaMessageReceiver.collector.get(test + "-topic.LDR").poll());
			assertKafkaMessage(message, kafkaMessageReceiver.collector.get(test + "-topic.LDR").poll());
			assertKafkaMessage(message, kafkaMessageReceiver.collector.get(test + "-topic.LDR").poll());
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isEmpty();

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();
		}
	}

	@ActiveProfiles({ "base-config-kafka-retry", "kafka-retry-dlt" })
	public static class KafkaRetryDlt extends BaseKafkaRetry
	{
		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder() //
					.put(test + "-topic", message -> throwIllegalArgumentException())
					.put(test + "-topic.DLT", message -> latch.countDown())
					.build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic", test + "-topic.DLT").doesNotContain(test + "-topic.SDR", test + "-topic.LDR");
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.executeInTransaction(kt -> {
				return kt.send(message);
			});
			latch.await(5, TimeUnit.SECONDS);

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
		}
	}

	@ActiveProfiles({ "base-config-kafka-retry", "kafka-retry-no-retries-topics-confgured" })
	public static class KafkaRetryNoRetriesTopicsConfigured extends BaseKafkaRetry
	{
		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder() //
					.put(test + "-topic", message -> throwIllegalArgumentException()).build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic").doesNotContain(test + "-topic.SDR", test + "-topic.LDR", test + "-topic.DLT");
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.executeInTransaction(kt -> {
				return kt.send(message);
			});
			latch.await(5, TimeUnit.SECONDS);

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).hasSize(3);
			assertKafkaMessage(message, kafkaMessageReceiver.collector.get(test + "-topic").poll());
			assertKafkaMessage(message, kafkaMessageReceiver.collector.get(test + "-topic").poll());
			assertKafkaMessage(message, kafkaMessageReceiver.collector.get(test + "-topic").poll());
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isEmpty();

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();
		}
	}

	@ActiveProfiles({ "base-config-kafka-retry", "kafka-retry-consumer-long-delay" })
	public static class KafkaRetryShortLongDltWithLongDelay extends BaseKafkaRetry
	{
		private CountDownLatch latch = new CountDownLatch(1);
		@Value("${caas.kafka.retryable-consumer.test-consumer-topic.short-delay-retry.message-delay-ms}")
		private long shortDelay;
		@Value("${caas.kafka.retryable-consumer.test-consumer-topic.long-delay-retry.message-delay-ms}")
		private long longDelay;
		private Map<String, Long> listenerTimestampMap = new ConcurrentHashMap<>();

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder() //
					.put(test + "-topic", message -> throwIllegalArgumentException()) //
					.put(test + "-topic.SDR", message -> {
						// save the timestamp when the Kafka Listener is invoked
						listenerTimestampMap.put(test + "-topic.SDR", gmtTimestamp());
						throwIllegalArgumentException();
					}) //
					.put(test + "-topic.LDR", message -> {
						// save the timestamp when the Kafka Listener is invoked
						listenerTimestampMap.put(test + "-topic.LDR", gmtTimestamp());
						throwIllegalArgumentException();
					}) //
					.put(test + "-topic.DLT", message -> latch.countDown()).build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic", test + "-topic.SDR", test + "-topic.LDR", test + "-topic.DLT");
		}

		@Test
		public void shouldAssertKafkaListenerExecutedAfterMessageDelay() throws Exception
		{
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).isNullOrEmpty();
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.executeInTransaction(kt -> {
				return kt.send(message);
			});
			latch.await(5, TimeUnit.SECONDS);

			final long sentTimestamp = message.getHeaders().get(KafkaHeaders.TIMESTAMP, Long.class);

			assertThat(kafkaMessageReceiver.collector.get(test + "-topic")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.SDR")).hasOnlyOneElementSatisfying(collectedMessage -> {
				assertKafkaMessage(message, collectedMessage);

				final long receivedTimestamp = collectedMessage.getHeaders().get(KafkaHeaders.RECEIVED_TIMESTAMP, Long.class);
				final long listenerTimestamp = listenerTimestampMap.get(test + "-topic.SDR").longValue();
				log.info("receivedTimestamp: {}", receivedTimestamp);
				log.info("gmtTimestamp:      {}", listenerTimestamp);
				assertThat(listenerTimestamp).isCloseTo(receivedTimestamp + shortDelay, within(100L));
			});
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.LDR")).hasOnlyOneElementSatisfying(collectedMessage -> {
				assertKafkaMessage(message, collectedMessage);

				final long receivedTimestamp = collectedMessage.getHeaders().get(KafkaHeaders.RECEIVED_TIMESTAMP, Long.class);
				final long listenerTimestamp = listenerTimestampMap.get(test + "-topic.LDR").longValue();
				log.info("receivedTimestamp: {}", receivedTimestamp);
				log.info("gmtTimestamp:      {}", listenerTimestamp);
				assertThat(listenerTimestamp).isCloseTo(receivedTimestamp + longDelay, within(200L));
			});
			assertThat(kafkaMessageReceiver.collector.get(test + "-topic.DLT")).hasOnlyOneElementSatisfying(
					collectedMessage -> assertKafkaMessage(message, collectedMessage));
		}
	}

	private static AbstractMapAssert<MapAssert<String, Object>, Map<String, Object>, String, Object> assertKafkaMessage(
			final Message<Map<String, String>> sentMessage, final Message<Map<String, String>> receivedMessage)
	{
		final MessageHeaders headers = sentMessage.getHeaders();

		assertThat(sentMessage).isNotNull();
		assertThat(receivedMessage).isNotNull();

		assertThat(receivedMessage.getPayload()).isEqualTo(sentMessage.getPayload());

		return assertThat(receivedMessage.getHeaders()) //
				.containsEntry(KafkaHeaders.RECEIVED_MESSAGE_KEY, headers.get(KafkaHeaders.MESSAGE_KEY))
				.containsEntry(CaasKafkaHeaders.TENANT, headers.get(CaasKafkaHeaders.TENANT))
				.containsEntry(CaasKafkaHeaders.MESSAGE_ID, headers.get(CaasKafkaHeaders.MESSAGE_ID));
	}

	/**
	 * GMT timestamp, same as used by kafka
	 */
	private static long gmtTimestamp()
	{
		return OffsetDateTime.now().toInstant().toEpochMilli();
	}

	private static void throwIllegalArgumentException()
	{
		log.info("you shall not pass!");
		throw new IllegalArgumentException("you shall not pass!");
	}

}
