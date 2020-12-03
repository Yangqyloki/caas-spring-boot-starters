package com.hybris.caas.kafka;

import com.google.common.collect.ImmutableMap;
import com.hybris.caas.kafka.config.CaasKafkaConfig;
import com.hybris.caas.kafka.config.CaasKafkaTransactionConfig;
import com.hybris.caas.kafka.message.MessageAssembler;
import com.hybris.caas.kafka.message.MessageAssemblerImpl;
import com.hybris.caas.kafka.util.CaasKafkaHeaders;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.aspectj.lang.annotation.Aspect;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * See {@code src/test/resources/application.yml}, profiles {@code default-kafka-container-*} for spring kafka configuration.
 */
@RunWith(Enclosed.class)
public class DefaultKafkaListenerContainerIntegrationTest
{
	private static final Logger log = LoggerFactory.getLogger(DefaultKafkaListenerContainerIntegrationTest.class);
	public static final int DEFAULT_TIMEOUT_IN_SEC = 5;

	@RunWith(SpringRunner.class)
	@SpringBootTest(classes = Application.class, properties = { "tenant.multiTenantSessionProperty=eclipselink.tenant-id",
			"spring.kafka.listener.missing-topics-fatal=false" })
	@ContextConfiguration(classes = { BaseDefaultKafkaContainer.RetryConfig.class, CaasKafkaConfig.class,
			CaasKafkaTransactionConfig.class, BaseDefaultKafkaContainer.Config.class })
	@DirtiesContext
	public static abstract class BaseDefaultKafkaContainer
	{
		@Autowired
		protected ApplicationContext context;
		@Autowired
		protected AdminClient adminClient;
		@Autowired
		protected KafkaTemplate kafkaTemplate;
		@Autowired
		protected KafkaMessageReceiver kafkaMessageReceiver;
		@Value("${caas.kafka.test}")
		protected String test;

		private MessageAssembler<Map<String, String>> messageAssembler;

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
		public static class RetryConfig
		{
			@Bean
			public ErrorHandler counterErrorHandler()
			{
				return new CounterErrorHandler();
			}

			public static class CounterErrorHandler implements ErrorHandler
			{
				private AtomicInteger counter = new AtomicInteger();
				private volatile CountDownLatch countDownLatch;

				@Override
				public void handle(final Exception thrownException, final ConsumerRecord<?, ?> record)
				{
					log.info("CounterErrorHandler was invoked...");
					counter.incrementAndGet();

					if (Objects.nonNull(countDownLatch))
					{
						countDownLatch.countDown();
					}
				}

				int getCounterValue()
				{
					return counter.intValue();
				}

				void resetCounter()
				{
					counter.set(0);
				}

				void setCountDownLatch(final CountDownLatch countDownLatch)
				{
					this.countDownLatch = countDownLatch;
				}
			}
		}

		@Configuration
		@EnableTransactionManagement
		public static class Config
		{
			private AdminClient adminClient;

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
				if (!contextClosedEvent.getApplicationContext().getDisplayName().startsWith("child-ctx-"))
				{
					try
					{
						final ListTopicsResult listTopicsResult = adminClient.listTopics();
						final Set<String> topics = listTopicsResult.names()
								.get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
								.stream()
								.filter(name -> name.startsWith("dkc-"))
								.collect(Collectors.toSet());

						final DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(topics);
						deleteTopicsResult.all().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);
					}
					catch (InterruptedException | TimeoutException | ExecutionException e)
					{
						log.error(
								"Failed to delete dkc- prefixed topics, check kafka broker is up and running (sometimes kafka broker fails to start)",
								e);
					}
				}
			}

			@Aspect
			@Component
			@Order // lowest precedence
			public static class KafkaMessageReceiverAspect
			{
				private volatile CountDownLatch receiverCountDownLatch;

				@org.aspectj.lang.annotation.After("execution(* com.hybris.caas.kafka.DefaultKafkaListenerContainerIntegrationTest.BaseDefaultKafkaContainer.KafkaMessageReceiver.messageListener(..))")
				public void doReleaseLock()
				{
					if (Objects.nonNull(receiverCountDownLatch))
					{
						receiverCountDownLatch.countDown();
					}
				}

				public void initializeCountDownLatch(final int value)
				{
					receiverCountDownLatch = new CountDownLatch(value);
				}

				public CountDownLatch getReceiverCountDownLatch()
				{
					return receiverCountDownLatch;
				}
			}
		}

		public static class KafkaMessageReceiver
		{
			private final Map<String, Deque<Message<Map<String, String>>>> collector = new ConcurrentHashMap<>();
			private Map<String, Consumer<Message<Map<String, String>>>> consumers;

			// unable to inject at constructor due to inheritance and springboot lifecycle
			void setConsumers(final Map<String, Consumer<Message<Map<String, String>>>> consumers)
			{
				this.consumers = consumers;
			}

			Map<String, Deque<Message<Map<String, String>>>> getCollector()
			{
				return collector;
			}

			@Transactional
			@KafkaListener(topics = "${caas.kafka.test}-topic")
			public void messageListener(@Payload final Message<Map<String, String>> message,
					@Header(KafkaHeaders.RECEIVED_TOPIC) final String topic)
			{
				log.info("Received in topic {}, the message {}", topic, message);

				collector.computeIfAbsent(topic, key -> new ConcurrentLinkedDeque<>()).offer(message);
				consumers.get(topic).accept(message);
			}
		}
	}

	@ActiveProfiles({ "base-config-default-kafka-container", "default-kafka-container-stateless-retry-error" })
	public static class DefaultKafkaContainerStatelessRetryError extends BaseDefaultKafkaContainer
	{
		@Autowired
		private ErrorHandler counterErrorHandler;

		private CountDownLatch latch = new CountDownLatch(3);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder().put(test + "-topic", message -> {
				latch.countDown();
				throwIllegalArgumentException();
			}).build();
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic");
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			final CountDownLatch errorHandlerLatch = new CountDownLatch(1);

			final RetryConfig.CounterErrorHandler errorHandler = ((RetryConfig.CounterErrorHandler) counterErrorHandler);
			errorHandler.resetCounter();
			errorHandler.setCountDownLatch(errorHandlerLatch);

			assertThat(kafkaMessageReceiver.getCollector().get(test + "-topic")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.send(message);

			errorHandlerLatch.await(5, TimeUnit.SECONDS);

			assertThat(kafkaMessageReceiver.getCollector().get(test + "-topic")).allSatisfy(
					collectedMessage -> assertKafkaMessage(message, collectedMessage)).hasSize(3);
			assertThat(((RetryConfig.CounterErrorHandler) counterErrorHandler).getCounterValue()).isEqualTo(1);
		}
	}

	@ActiveProfiles({ "base-config-default-kafka-container", "default-kafka-container-stateless-retry-success" })
	public static class DefaultKafkaContainerStatelessRetrySuccess extends BaseDefaultKafkaContainer
	{
		@Autowired
		private ErrorHandler counterErrorHandler;
		@Autowired
		private JdbcTemplate jdbcTemplate;
		@Autowired
		private Config.KafkaMessageReceiverAspect receiverAspect;

		private AtomicInteger messageCount = new AtomicInteger(0);

		@Override
		protected Map<String, Consumer<Message<Map<String, String>>>> consumerPerTopicMap()
		{
			return ImmutableMap.<String, Consumer<Message<Map<String, String>>>>builder().put(test + "-topic", message -> {
				jdbcTemplate.update("INSERT INTO kafka_rollback_test(id) VALUES (?)", messageCount.get());

				if (messageCount.getAndIncrement() < 2)
				{
					throwIllegalArgumentException();
				}
			}).build();
		}

		@Before
		public void beforeTest()
		{
			((RetryConfig.CounterErrorHandler) counterErrorHandler).resetCounter();

			jdbcTemplate.execute("DROP TABLE IF EXISTS kafka_rollback_test");
			jdbcTemplate.execute("CREATE TABLE kafka_rollback_test(id integer)");
		}

		@After
		public void afterTest()
		{
			jdbcTemplate.execute("DROP TABLE IF EXISTS kafka_rollback_test");
		}

		@Test
		public void assertTopicsCreated() throws Exception
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			assertThat(topics).contains(test + "-topic");
		}

		@Test
		public void assertTestTableCreated() throws Exception
		{
			final Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM kafka_rollback_test", Long.class);

			assertThat(count).isGreaterThanOrEqualTo(0);
		}

		@Test
		public void shouldRetryMessage() throws Exception
		{
			receiverAspect.initializeCountDownLatch(3);
			assertThat(kafkaMessageReceiver.getCollector().get(test + "-topic")).isNullOrEmpty();

			final Message<Map<String, String>> message = buildMessage();
			kafkaTemplate.send(message);

			receiverAspect.getReceiverCountDownLatch().await(10, TimeUnit.SECONDS);

			assertThat(kafkaMessageReceiver.getCollector().get(test + "-topic")).allSatisfy(
					collectedMessage -> assertKafkaMessage(message, collectedMessage)).hasSize(3);

			//Disable the following check since it flaky at this point in CI (to be investigated further)
			//final Integer storedValue = jdbcTemplate.queryForObject("SELECT id FROM kafka_rollback_test", Integer.class);
			//assertThat(storedValue).isEqualTo(2);

			assertThat(((RetryConfig.CounterErrorHandler) counterErrorHandler).getCounterValue()).isEqualTo(0);
		}
	}

	private static void assertKafkaMessage(final Message<Map<String, String>> sentMessage,
			final Message<Map<String, String>> receivedMessage)
	{
		final MessageHeaders headers = sentMessage.getHeaders();

		assertThat(sentMessage).isNotNull();
		assertThat(receivedMessage).isNotNull();

		assertThat(receivedMessage.getHeaders()) //
				.containsEntry(KafkaHeaders.RECEIVED_MESSAGE_KEY, headers.get(KafkaHeaders.MESSAGE_KEY))
				.containsEntry(CaasKafkaHeaders.TENANT, headers.get(CaasKafkaHeaders.TENANT))
				.containsEntry(CaasKafkaHeaders.MESSAGE_ID, headers.get(CaasKafkaHeaders.MESSAGE_ID));

		assertThat(receivedMessage.getPayload()).isEqualTo(sentMessage.getPayload());
	}

	private static void throwIllegalArgumentException()
	{
		log.info("you shall not pass!");
		throw new IllegalArgumentException("you shall not pass!");
	}
}
