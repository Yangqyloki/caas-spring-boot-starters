package com.hybris.caas.kafka.config;

import com.hybris.caas.error.converter.ExceptionConverter;
import com.hybris.caas.error.converter.ExceptionConverterFactory;
import com.hybris.caas.kafka.error.RetryableConsumerDeadLetterPublishingRecoverer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hybris.caas.kafka.config.CaasKafkaConfig.CONTAINER_TRANSACTION_ID_PREFIX;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaasKafkaConfigTest
{
	private static final String PRODUCER_1 = "producer-1";
	private static final String PRODUCER_2 = "producer-2";
	private static final String DESTINATION_1 = "destination-1";
	private static final String DESTINATION_2 = "destination-2";
	private static final String CONSUMER_1 = "consumer-1";
	private static final String RETRYABLE_CONSUMER_1 = "retryable-consumer-1";
	private static final String SOURCE_1 = "source-1";
	private static final String SOURCE_2 = "source-2";
	private static final String RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX = "retryable-consumer-tx-";

	@Mock
	private ConfigurableListableBeanFactory beanFactory;
	@Mock
	private BeanDefinitionRegistry beanDefinitionRegistry;
	@Mock
	private ErrorHandler errorHandler;
	@Mock
	private ConcurrentKafkaListenerContainerFactoryConfigurer configurer;
	@Mock
	private ConsumerFactory<Object, Object> kafkaConsumerFactory;
	@Mock
	private RecordMessageConverter messageConverter;
	@Mock
	private ProducerFactory<Object, Object> producerFactory;
	@Mock
	private ProducerListener<Object, Object> producerListener;
	@Mock
	private ObjectProvider<RecordMessageConverter> providerMessageConverter;
	@Mock
	private CaasKafkaConfig.ContainerKafkaTemplate containerKafkaTemplate;
	@Mock
	private CaasKafkaConfig.ContainerKafkaTransactionManager containerKafkaTransactionManager;
	@Mock
	private RetryableConsumerDeadLetterPublishingRecoverer retryableConsumerDeadLetterPublishingRecoverer;
	@Mock
	private KafkaTransactionManager<?,?> transactionManager;
	@Mock
	private KafkaTemplate<Object, Object> kafkaTemplate;
	@Mock
	private ExceptionConverter exceptionConverter;

	private final KafkaProperties kafkaProperties = new KafkaProperties();
	private final CaasKafkaProperties caasKafkaProperties = new CaasKafkaProperties();
	private final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap = Collections.emptyMap();
	private final Map<Class<? extends Throwable>, Boolean> kafkaListenerRetryExceptionsMap = Collections.singletonMap(Exception.class, true);

	private CaasKafkaConfig caasKafkaConfig;

	@Before
	public void setUp()
	{
		final CaasKafkaProperties.Producer producer1 = new CaasKafkaProperties.Producer();
		producer1.setDestination(DESTINATION_1);
		final CaasKafkaProperties.Producer producer2 = new CaasKafkaProperties.Producer();
		producer2.setDestination(DESTINATION_2);
		final CaasKafkaProperties.Consumer consumer1 = new CaasKafkaProperties.Consumer();
		consumer1.setSource(SOURCE_1);
		consumer1.setPartitionCount(1);
		final CaasKafkaProperties.RetryableConsumer retryableConsumer1 = new CaasKafkaProperties.RetryableConsumer();
		retryableConsumer1.setSource(SOURCE_2);
		retryableConsumer1.setPartitionCount(1);
		retryableConsumer1.setShortDelayRetry(new CaasKafkaProperties.RetryTopic());
		retryableConsumer1.setLongDelayRetry(new CaasKafkaProperties.RetryTopic());
		retryableConsumer1.setDeadLetter(new CaasKafkaProperties.DeadLetterTopic());

		caasKafkaProperties.getProducer().put(PRODUCER_1, producer1);
		caasKafkaProperties.getProducer().put(PRODUCER_2, producer2);
		caasKafkaProperties.getConsumer().put(CONSUMER_1, consumer1);
		caasKafkaProperties.getRetryableConsumer().put(RETRYABLE_CONSUMER_1, retryableConsumer1);
		caasKafkaProperties.getListener().getRetryableConsumer().setTransactionIdPrefix(RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX);

		caasKafkaConfig = new CaasKafkaConfig(caasKafkaProperties, kafkaProperties, exceptionConverter)
		{
			@Override
			BeanDefinitionRegistry buildBeanDefinitionRegistry(final ConfigurableListableBeanFactory beanFactory)
			{
				return beanDefinitionRegistry;
			}
		};

		when(containerKafkaTemplate.getKafkaTemplate()).thenReturn(kafkaTemplate);
		when(containerKafkaTransactionManager.getKafkaTransactionManager()).thenReturn(transactionManager);
	}

	@Test
	public void should_register_NewTopic_beans_for_producers_and_retryable_consumers()
	{
		caasKafkaConfig.createKafkaTopicBeans(beanFactory);
		verify(beanDefinitionRegistry, times(6)).registerBeanDefinition(anyString(), any());
	}

	@Test
	public void should_register_NewTopic_beans_for_producers_and_consumers_and_retryable_consumers()
	{
		caasKafkaProperties.setCreateConsumerTopics(true);
		caasKafkaConfig.createKafkaTopicBeans(beanFactory);
		verify(beanDefinitionRegistry, times(7)).registerBeanDefinition(anyString(), any());
	}

	@Test
	public void should_create_kafkaConsumerFactory_preconfigured()
	{
		final ConsumerFactory<?, ?> consumerFactory = caasKafkaConfig.kafkaConsumerFactory();
		final Map<String, Object> config = consumerFactory.getConfigurationProperties();
		assertThat(config, hasEntry(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName()));
		assertFalse(consumerFactory.isAutoCommit());
	}

	@Test
	public void should_set_retry_template_in_kafkaListenerContainerFactory()
	{
		final RecoveryCallback recoveryCallback = mock(RecoveryCallback.class);
		caasKafkaProperties.getListener().getRetry().setEnabled(true);

		final ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory = caasKafkaConfig.kafkaListenerContainerFactory(
				configurer, kafkaConsumerFactory, messageConverter, errorHandler, kafkaListenerRetryExceptionsMap,
				Optional.of(recoveryCallback));

		assertThat(kafkaListenerContainerFactory.getContainerProperties().getAckMode(), is(ContainerProperties.AckMode.RECORD));

		org.assertj.core.api.Assertions.assertThat(kafkaListenerContainerFactory).extracting("statefulRetry").isEqualTo(false);
		org.assertj.core.api.Assertions.assertThat(kafkaListenerContainerFactory)
				.extracting("retryTemplate.retryPolicy")
				.isInstanceOf(SimpleRetryPolicy.class);
		org.assertj.core.api.Assertions.assertThat(kafkaListenerContainerFactory)
				.extracting("recoveryCallback")
				.isSameAs(recoveryCallback);
	}

	@Test
	public void should_set_container_transactionIdPrefix_for_container_template()
	{
		final CaasKafkaConfig.ContainerKafkaTemplate containerKafkaTemplate = caasKafkaConfig.containerKafkaTemplate(producerFactory,
				producerListener, providerMessageConverter, kafkaProperties, caasKafkaProperties);

		assertThat(containerKafkaTemplate.getKafkaTemplate().getTransactionIdPrefix(),
				is(RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX + CONTAINER_TRANSACTION_ID_PREFIX));
	}

	@Test
	public void should_set_container_transactionIdPrefix_for_container_transaction_manager()
	{
		when(producerFactory.transactionCapable()).thenReturn(Boolean.TRUE);

		final CaasKafkaConfig.ContainerKafkaTransactionManager containerKafkaTransactionManager = caasKafkaConfig.containerKafkaTransactionManager(
				producerFactory, caasKafkaProperties);

		assertThat(containerKafkaTransactionManager.getTransactionIdPrefix(),
				is(RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX + CONTAINER_TRANSACTION_ID_PREFIX));
	}

	@Test
	public void should_build_retryable_consumer_map()
	{
		final CaasKafkaProperties.RetryableConsumer retryableConsumer1 = new CaasKafkaProperties.RetryableConsumer();
		retryableConsumer1.setSource(SOURCE_1);

		final CaasKafkaProperties.RetryableConsumer retryableConsumer2 = new CaasKafkaProperties.RetryableConsumer();
		retryableConsumer2.setSource("dummy");

		final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap = Stream.of(
				new AbstractMap.SimpleEntry<>("c1", retryableConsumer1), new AbstractMap.SimpleEntry<>("c2", retryableConsumer2))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		caasKafkaProperties.setRetryableConsumerMap(retryableConsumerMap);

		final Map<String, CaasKafkaProperties.RetryableConsumer> result = caasKafkaConfig.retryableConsumerMap();

		assertThat(result.size(), is(2));
		assertThat(result.get(SOURCE_1), is(retryableConsumer1));
		assertThat(result.get("dummy"), is(retryableConsumer2));
	}

	@Test(expected = IllegalStateException.class)
	public void should_throw_IllegalStateException_when_2_retryable_consumers_have_same_source()
	{
		final CaasKafkaProperties.RetryableConsumer retryableConsumer1 = new CaasKafkaProperties.RetryableConsumer();
		retryableConsumer1.setSource(SOURCE_1);

		final CaasKafkaProperties.RetryableConsumer retryableConsumer2 = new CaasKafkaProperties.RetryableConsumer();
		retryableConsumer2.setSource(SOURCE_1);

		final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap = Stream.of(
				new AbstractMap.SimpleEntry<>("c1", retryableConsumer1), new AbstractMap.SimpleEntry<>("c2", retryableConsumer2))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		caasKafkaProperties.setRetryableConsumerMap(retryableConsumerMap);

		caasKafkaConfig.retryableConsumerMap();
	}

	@Test
	public void should_create_retryableKafkaListenerContainerFactory_without_retry_template()
	{
		final ConcurrentKafkaListenerContainerFactory<?, ?> retryableKafkaListenerContainerFactory = caasKafkaConfig.retryableKafkaListenerContainerFactory(
				configurer, kafkaConsumerFactory, messageConverter, containerKafkaTemplate, containerKafkaTransactionManager,
				retryableConsumerDeadLetterPublishingRecoverer, retryableConsumerMap, kafkaListenerRetryExceptionsMap,
				Optional.empty());

		assertFalse(retryableKafkaListenerContainerFactory.getContainerProperties().isAckOnError());
		assertThat(retryableKafkaListenerContainerFactory.getContainerProperties().getAckMode(),
				is(ContainerProperties.AckMode.RECORD));
		assertTrue(retryableKafkaListenerContainerFactory.getContainerProperties().isSyncCommits());

		org.assertj.core.api.Assertions.assertThat(retryableKafkaListenerContainerFactory).extracting("statefulRetry").isNull();
		org.assertj.core.api.Assertions.assertThat(retryableKafkaListenerContainerFactory).extracting("replyTemplate").isNull();

		assertThat(retryableKafkaListenerContainerFactory.getContainerProperties().getTransactionManager(), is(transactionManager));
	}

	@Test
	public void should_create_retryableKafkaListenerContainerFactory_with_retry_template()
	{
		final RecoveryCallback recoveryCallback = mock(RecoveryCallback.class);
		caasKafkaProperties.getListener().getRetry().setEnabled(true);

		final ConcurrentKafkaListenerContainerFactory<?, ?> retryableKafkaListenerContainerFactory = caasKafkaConfig.retryableKafkaListenerContainerFactory(
				configurer, kafkaConsumerFactory, messageConverter, containerKafkaTemplate, containerKafkaTransactionManager,
				retryableConsumerDeadLetterPublishingRecoverer, retryableConsumerMap, kafkaListenerRetryExceptionsMap,
				Optional.of(recoveryCallback));

		org.assertj.core.api.Assertions.assertThat(retryableKafkaListenerContainerFactory)
				.extracting("statefulRetry")
				.isEqualTo(false);
		org.assertj.core.api.Assertions.assertThat(retryableKafkaListenerContainerFactory)
				.extracting("retryTemplate.retryPolicy")
				.isInstanceOf(SimpleRetryPolicy.class);
		org.assertj.core.api.Assertions.assertThat(retryableKafkaListenerContainerFactory)
				.extracting("recoveryCallback")
				.isSameAs(recoveryCallback);
	}

	@Test
	public void should_create_default_exceptions_map_for_listener_retry_template()
	{
		final Map<Class<? extends Throwable>, Boolean> exceptionsMap = caasKafkaConfig.kafkaListenerRetryExceptionsMap();

		assertThat(exceptionsMap.size(), is(1));
		assertThat(exceptionsMap.get(Exception.class), is(Boolean.TRUE));
	}
}
