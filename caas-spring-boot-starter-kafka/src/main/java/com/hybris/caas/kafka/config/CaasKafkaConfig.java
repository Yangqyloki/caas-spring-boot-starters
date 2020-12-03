package com.hybris.caas.kafka.config;

import brave.Tracer;
import brave.kafka.clients.KafkaTracing;
import com.hybris.caas.error.converter.ExceptionConverter;
import com.hybris.caas.kafka.error.CaasKafkaLoggingErrorHandler;
import com.hybris.caas.kafka.error.RetryableConsumerDeadLetterPublishingRecoverer;
import com.hybris.caas.kafka.error.RetryableConsumerDestinationTopicResolver;
import com.hybris.caas.kafka.interceptor.RetryableConsumerAfterRollbackProcessorDecorator;
import com.hybris.caas.kafka.interceptor.RetryableConsumerErrorHandler;
import com.hybris.caas.kafka.interceptor.RetryableConsumerRecordInterceptor;
import com.hybris.caas.kafka.tracing.ConsumerRecordTracing;
import com.hybris.caas.kafka.transaction.SyncKafkaTemplate;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.BatchLoggingErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultAfterRollbackProcessor;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.backoff.FixedBackOff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hybris.caas.kafka.util.CaasKafkaConstants.DEAD_LETTER_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.IDLE_BETWEEN_RETRY_POLLS_MS;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.LONG_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.SHORT_DELAY_RETRY_TOPIC_SUFFIX;

/**
 * Spring configuration for Kafka.
 */
@EnableKafka
@Configuration
@EnableConfigurationProperties(CaasKafkaProperties.class)
@AutoConfigureBefore(KafkaAutoConfiguration.class)
@PropertySource("classpath:kafka.properties")
public class CaasKafkaConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(CaasKafkaConfig.class);

	static final String CONTAINER_TRANSACTION_ID_PREFIX = "container-";

	private final CaasKafkaProperties caasKafkaProperties;
	private final KafkaProperties kafkaProperties;
	private final ExceptionConverter exceptionConverter;

	public CaasKafkaConfig(final CaasKafkaProperties caasKafkaProperties, final KafkaProperties kafkaProperties,
			@Qualifier("exceptionConverterFactory") final ExceptionConverter exceptionConverter)
	{
		this.caasKafkaProperties = caasKafkaProperties;
		this.kafkaProperties = kafkaProperties;
		this.exceptionConverter = exceptionConverter;
	}

	@Bean
	public Void createKafkaTopicBeans(final ConfigurableListableBeanFactory beanDefinitionRegistry)
	{
		// Dynamically create the Kafka topic beans.
		final BeanDefinitionRegistry registry = buildBeanDefinitionRegistry(beanDefinitionRegistry);

		final Collection<CaasKafkaProperties.Producer> bindings = new HashSet<>(caasKafkaProperties.getProducer().values());

		// should we create the topics for the consumers configured under: caas.kafka.consumers
		if (caasKafkaProperties.isCreateConsumerTopics())
		{
			caasKafkaProperties.getConsumer().values().stream().map(this::createProducerForConsumer).forEach(bindings::add);
		}

		// create the topics for the consumers configured under: caas.kafka.retryable-consumer
		// and their short delay, long delay or dead letter topics, if configured.
		caasKafkaProperties.getRetryableConsumer()
				.values()
				.stream()
				.map(this::createProducerForRetryableConsumer)
				.forEach(bindings::addAll);

		bindings.forEach(binding -> {

			final String topicName = binding.getDestination();
			final ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
			constructorArgumentValues.addIndexedArgumentValue(0, topicName, "String");
			constructorArgumentValues.addIndexedArgumentValue(1, binding.getPartitionCount(), "int");
			constructorArgumentValues.addIndexedArgumentValue(2, binding.getReplicationFactor(), "short");

			final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
			beanDefinition.setBeanClass(NewTopic.class);
			beanDefinition.setConstructorArgumentValues(constructorArgumentValues);

			LOG.info("CaaS Kafka - registering Kafka topic bean for topic: '{}'", topicName);
			registry.registerBeanDefinition("caasKafkaTopic-" + binding.getDestination(), beanDefinition);
		});
		return null;
	}

	private CaasKafkaProperties.Producer createProducerForConsumer(final CaasKafkaProperties.Consumer consumer)
	{
		final CaasKafkaProperties.Producer producer = new CaasKafkaProperties.Producer();
		producer.setDestination(consumer.getSource());
		producer.setPartitionCount(consumer.getPartitionCount());
		producer.setReplicationFactor(consumer.getReplicationFactor());
		return producer;
	}

	private List<CaasKafkaProperties.Producer> createProducerForRetryableConsumer(final CaasKafkaProperties.RetryableConsumer consumer)
	{
		final List<CaasKafkaProperties.Producer> producers = new ArrayList<>();

		producers.add(createProducerForTopic(consumer.getSource(), consumer));

		final Optional<CaasKafkaProperties.RetryTopic> shortDelayRetry = Optional.ofNullable(consumer.getShortDelayRetry());
		if (shortDelayRetry.isPresent())
		{
			final String topicName = shortDelayRetry.map(CaasKafkaProperties.RetryTopic::getTopicPrefix).orElse(consumer.getSource())
					+ SHORT_DELAY_RETRY_TOPIC_SUFFIX;
			producers.add(createProducerForTopic(topicName, shortDelayRetry.get()));
		}

		final Optional<CaasKafkaProperties.RetryTopic> longDelayRetry = Optional.ofNullable(consumer.getLongDelayRetry());
		if (longDelayRetry.isPresent())
		{
			final String topicName = longDelayRetry.map(CaasKafkaProperties.RetryTopic::getTopicPrefix).orElse(consumer.getSource())
					+ LONG_DELAY_RETRY_TOPIC_SUFFIX;
			producers.add(createProducerForTopic(topicName, longDelayRetry.get()));
		}

		final Optional<CaasKafkaProperties.DeadLetterTopic> deadLetter = Optional.ofNullable(consumer.getDeadLetter());
		if (deadLetter.isPresent())
		{
			final String topicName = deadLetter.map(CaasKafkaProperties.DeadLetterTopic::getTopicPrefix).orElse(consumer.getSource())
					+ DEAD_LETTER_TOPIC_SUFFIX;
			producers.add(createProducerForTopic(topicName, deadLetter.get()));
		}

		return producers;
	}

	private CaasKafkaProperties.Producer createProducerForTopic(final String name, final CaasKafkaProperties.Topic topic)
	{
		final CaasKafkaProperties.Producer producer = new CaasKafkaProperties.Producer();
		producer.setDestination(name);
		producer.setPartitionCount(topic.getPartitionCount());
		producer.setReplicationFactor(topic.getReplicationFactor());
		return producer;
	}

	@Bean
	public SyncKafkaTemplate syncKafkaTemplate(final KafkaTemplate kafkaTemplate)
	{
		return new SyncKafkaTemplate(kafkaTemplate, caasKafkaProperties);
	}

	@Bean
	@ConditionalOnMissingBean(ConsumerFactory.class)
	@SuppressWarnings("squid:S1452")
	public ConsumerFactory<?, ?> kafkaConsumerFactory()
	{
		final KafkaProperties.Consumer consumer = kafkaProperties.getConsumer();

		// register the round robin assignment strategy
		consumer.getProperties().put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());

		// disable auto commit
		consumer.setEnableAutoCommit(false);

		return new DefaultKafkaConsumerFactory<>(this.kafkaProperties.buildConsumerProperties());
	}

	@Bean
	@ConditionalOnMissingBean(name = "kafkaBatchListenerContainerFactory")
	@ConditionalOnProperty(name = "caas.kafka.batch.enabled", havingValue = "true")
	@SuppressWarnings("squid:S1452")
	public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaBatchListenerContainerFactory(
			final ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
			final ConsumerFactory<Object, Object> kafkaConsumerFactory, final RecordMessageConverter messageConverter)
	{
		LOG.info("CaaS Kafka - registering batch concurrent kafka listener container");

		final ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		configurer.configure(factory, kafkaConsumerFactory);

		factory.setConcurrency(caasKafkaProperties.getBatch().getConcurrency());
		factory.setMessageConverter(new BatchMessagingMessageConverter(messageConverter));
		factory.setBatchListener(true);
		// register error handler
		factory.setBatchErrorHandler(new BatchLoggingErrorHandler());

		final ContainerProperties containerProperties = factory.getContainerProperties();
		containerProperties.setTransactionManager(null);
		containerProperties.setAckMode(ContainerProperties.AckMode.BATCH);

		return factory;
	}

	@Bean
	@ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
	@SuppressWarnings("squid:S1452")
	public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
			final ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
			final ConsumerFactory<Object, Object> kafkaConsumerFactory, final RecordMessageConverter messageConverter,
			final ErrorHandler errorHandler, final Map<Class<? extends Throwable>, Boolean> kafkaListenerRetryExceptionsMap,
			final Optional<RecoveryCallback<?>> optionalRecoveryCallback)
	{
		LOG.info("A default concurrent kafka listener container factory bean is being instantiated");

		final ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		configurer.configure(factory, kafkaConsumerFactory);

		factory.setConcurrency(calculateContainerConcurrency());
		factory.setMessageConverter(messageConverter);
		// register error handler
		factory.setErrorHandler(errorHandler);

		// enable and configure retry on failed message processing, if enabled via caas.kafka.listener.retry.enabled: true
		final CaasKafkaProperties.Retry retry = caasKafkaProperties.getListener().getRetry();
		if (retry.isEnabled())
		{
			factory.setRetryTemplate(retryTemplate(retry, kafkaListenerRetryExceptionsMap));
			optionalRecoveryCallback.ifPresent(factory::setRecoveryCallback);
			factory.setStatefulRetry(false);
		}

		final ContainerProperties containerProperties = factory.getContainerProperties();
		containerProperties.setTransactionManager(null);
		// configure record-based acknowledgment
		containerProperties.setAckMode(AckMode.RECORD);
		return factory;
	}

	@Bean
	@ConditionalOnProperty(name = "spring.kafka.producer.transaction-id-prefix")
	@ConditionalOnExpression("!'${caas.kafka.listener.retryable-consumer.transaction-id-prefix:}'.isEmpty()")
	ContainerKafkaTransactionManager containerKafkaTransactionManager(final ProducerFactory<?, ?> producerFactory,
			final CaasKafkaProperties caasKafkaProperties)
	{
		final String retryableConsumerTransactionIdPrefix = getRetryableConsumerContainerTransactionIdPrefix(caasKafkaProperties);

		LOG.info(
				"CaaS Kafka - registering containerKafkaTransactionManager bean for retryable consumer with {} transaction identifier prefix",
				retryableConsumerTransactionIdPrefix);

		return new ContainerKafkaTransactionManager(producerFactory, retryableConsumerTransactionIdPrefix);
	}

	@Bean
	@ConditionalOnProperty(name = "spring.kafka.producer.transaction-id-prefix")
	@ConditionalOnExpression("!'${caas.kafka.listener.retryable-consumer.transaction-id-prefix:}'.isEmpty()")
	ContainerKafkaTemplate containerKafkaTemplate(final ProducerFactory<Object, Object> producerFactory,
			final ProducerListener<Object, Object> kafkaProducerListener,
			final ObjectProvider<RecordMessageConverter> messageConverter, final KafkaProperties kafkaProperties,
			final CaasKafkaProperties caasKafkaProperties)
	{
		LOG.info("CaaS Kafka - registering containerKafkaTemplate bean for retryable consumer");

		return new ContainerKafkaTemplate(producerFactory, kafkaProducerListener, messageConverter, kafkaProperties,
				caasKafkaProperties);
	}

	@Bean
	@ConditionalOnProperty(name = "spring.kafka.producer.transaction-id-prefix")
	@ConditionalOnExpression("!'${caas.kafka.listener.retryable-consumer.transaction-id-prefix:}'.isEmpty()")
	RetryableConsumerDeadLetterPublishingRecoverer retryableConsumerDeadLetterPublishingRecoverer(
			final ContainerKafkaTemplate containerKafkaTemplate,
			final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap)
	{
		return new RetryableConsumerDeadLetterPublishingRecoverer(containerKafkaTemplate.getKafkaTemplate(),
				new RetryableConsumerDestinationTopicResolver(retryableConsumerMap)::getTopicPartition);
	}

	@Bean
	@ConditionalOnProperty(name = "spring.kafka.producer.transaction-id-prefix")
	@ConditionalOnExpression("!'${caas.kafka.listener.retryable-consumer.transaction-id-prefix:}'.isEmpty()")
	Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap()
	{
		final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap = Collections.unmodifiableMap(caasKafkaProperties
				.getRetryableConsumer()
				.values()
				.stream()
				.collect(Collectors.toMap(CaasKafkaProperties.RetryableConsumer::getSource, Function.identity())));

		if (caasKafkaProperties.getRetryableConsumer().size() != retryableConsumerMap.size())
		{
			throw new IllegalStateException("Invalid retryable-consumer configuration (multiple consumers found with same source).");
		}

		return retryableConsumerMap;
	}

	@Bean
	@ConditionalOnProperty(name = "spring.kafka.producer.transaction-id-prefix")
	@ConditionalOnExpression("!'${caas.kafka.listener.retryable-consumer.transaction-id-prefix:}'.isEmpty()")
	@SuppressWarnings({ "squid:S1452", "squid:S00107" }) // too many params
	public ConcurrentKafkaListenerContainerFactory<?, ?> retryableKafkaListenerContainerFactory(
			final ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
			final ConsumerFactory<Object, Object> kafkaConsumerFactory, final RecordMessageConverter messageConverter,
			final ContainerKafkaTemplate containerKafkaTemplate,
			final ContainerKafkaTransactionManager containerKafkaTransactionManager,
			final RetryableConsumerDeadLetterPublishingRecoverer retryableConsumerDeadLetterPublishingRecoverer,
			final Map<String, CaasKafkaProperties.RetryableConsumer> retryableConsumerMap,
			final Map<Class<? extends Throwable>, Boolean> kafkaListenerRetryExceptionsMap,
			final Optional<RecoveryCallback<?>> optionalRecoveryCallback)
	{
		LOG.info("A Retryable concurrent kafka listener container factory bean is being instantiated");
		final ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		configurer.configure(factory, kafkaConsumerFactory);

		final CaasKafkaProperties.RetryableConsumerListener retryableConsumerListener = caasKafkaProperties.getListener()
				.getRetryableConsumer();

		// register after rollback processor
		final DefaultAfterRollbackProcessor<Object, Object> rollbackProcessor = new DefaultAfterRollbackProcessor<>(null,
				new FixedBackOff(0, retryableConsumerListener.getMaxAttempts()), containerKafkaTemplate.getKafkaTemplate(), true);

		factory.setAfterRollbackProcessor(new RetryableConsumerAfterRollbackProcessorDecorator<>(rollbackProcessor));

		factory.setConcurrency(retryableConsumerListener.getConcurrency());
		factory.setMessageConverter(messageConverter);

		// register record interceptor
		factory.setRecordInterceptor(new RetryableConsumerRecordInterceptor<>(retryableConsumerMap, IDLE_BETWEEN_RETRY_POLLS_MS));

		// register error handler
		factory.setErrorHandler(new RetryableConsumerErrorHandler(retryableConsumerDeadLetterPublishingRecoverer));

		// enable and configure retry on failed message processing, if enabled via caas.kafka.listener.retry.enabled: true
		final CaasKafkaProperties.Retry retry = caasKafkaProperties.getListener().getRetry();
		if (retry.isEnabled())
		{
			factory.setRetryTemplate(retryTemplate(retry, kafkaListenerRetryExceptionsMap));
			optionalRecoveryCallback.ifPresent(factory::setRecoveryCallback);
			factory.setStatefulRetry(false);
		}

		final ContainerProperties containerProperties = factory.getContainerProperties();
		containerProperties.setTransactionManager(containerKafkaTransactionManager.getKafkaTransactionManager());

		// configure record-based acknowledgment
		containerProperties.setAckMode(AckMode.RECORD);
		containerProperties.setSyncCommits(true);

		return factory;
	}

	private static String getRetryableConsumerContainerTransactionIdPrefix(final CaasKafkaProperties caasKafkaProperties)
	{
		return caasKafkaProperties.getListener().getRetryableConsumer().getTransactionIdPrefix() + CONTAINER_TRANSACTION_ID_PREFIX;
	}

	public static class ContainerKafkaTemplate
	{
		private final KafkaTemplate<Object, Object> kafkaTemplate;

		public ContainerKafkaTemplate(final ProducerFactory<Object, Object> producerFactory,
				final ProducerListener<Object, Object> producerListener, final ObjectProvider<RecordMessageConverter> messageConverter,
				final KafkaProperties kafkaProperties, final CaasKafkaProperties caasKafkaProperties)
		{
			final KafkaTemplate<Object, Object> kafkaTemplateTemp = new KafkaTemplate<>(producerFactory);

			messageConverter.ifUnique(kafkaTemplateTemp::setMessageConverter);
			kafkaTemplateTemp.setProducerListener(producerListener);
			kafkaTemplateTemp.setDefaultTopic(kafkaProperties.getTemplate().getDefaultTopic());
			kafkaTemplateTemp.setTransactionIdPrefix(getRetryableConsumerContainerTransactionIdPrefix(caasKafkaProperties));

			this.kafkaTemplate = kafkaTemplateTemp;
		}

		KafkaTemplate<Object, Object> getKafkaTemplate()
		{
			return this.kafkaTemplate;
		}
	}

	public static class ContainerKafkaTransactionManager
	{
		private final KafkaTransactionManager kafkaTransactionManager;
		private final String transactionIdPrefix;

		public ContainerKafkaTransactionManager(final ProducerFactory<?, ?> producerFactory, final String transactionIdPrefix)
		{
			this.transactionIdPrefix = transactionIdPrefix;

			final KafkaTransactionManager kafkaTransactionManagerTemp = new KafkaTransactionManager<>(producerFactory);
			kafkaTransactionManagerTemp.setTransactionIdPrefix(this.transactionIdPrefix);

			this.kafkaTransactionManager = kafkaTransactionManagerTemp;
		}

		KafkaTransactionManager getKafkaTransactionManager()
		{
			return this.kafkaTransactionManager;
		}

		String getTransactionIdPrefix()
		{
			return this.transactionIdPrefix;
		}
	}

	@Bean
	@ConditionalOnMissingBean
	public ErrorHandler defaultErrorHandler()
	{
		return new CaasKafkaLoggingErrorHandler(exceptionConverter);
	}

	@Bean
	@ConditionalOnProperty(name = "caas.kafka.batch.enabled", havingValue = "true")
	public ConsumerRecordTracing consumerRecordTracing(final Tracer tracer, final KafkaTracing kafkaTracing,
			final RecordMessageConverter messageConverter)
	{
		return new ConsumerRecordTracing(tracer, kafkaTracing, messageConverter);
	}

	@Bean
	@ConditionalOnMissingBean
	public RecordMessageConverter messageConverter()
	{
		return new StringJsonMessageConverter();
	}

	@Bean
	@ConditionalOnMissingBean(name = "kafkaListenerRetryExceptionsMap")
	public Map<Class<? extends Throwable>, Boolean> kafkaListenerRetryExceptionsMap()
	{
		return Collections.singletonMap(Exception.class, true);
	}

	private Integer calculateContainerConcurrency()
	{
		final int amountOfConsumer = caasKafkaProperties.getConsumer()
				.values()
				.stream()
				.mapToInt(CaasKafkaProperties.Consumer::getPartitionCount)
				.sum();

		return amountOfConsumer > 0 ? amountOfConsumer : null;
	}

	BeanDefinitionRegistry buildBeanDefinitionRegistry(final ConfigurableListableBeanFactory beanFactory)
	{
		return (BeanDefinitionRegistry) beanFactory;
	}

	private RetryTemplate retryTemplate(final CaasKafkaProperties.Retry retry,
			final Map<Class<? extends Throwable>, Boolean> retryExceptionsMap)
	{
		final RetryTemplate template = new RetryTemplate();
		template.setRetryPolicy(new SimpleRetryPolicy(retry.getMaxAttempts(), retryExceptionsMap, retry.isTraverseExceptionCauses(),
				retry.isRetryExceptionDefaultValue()));
		template.setBackOffPolicy(backOffPolicy(retry));
		return template;
	}

	private BackOffPolicy backOffPolicy(final CaasKafkaProperties.Retry retry)
	{
		final ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
		policy.setInitialInterval(retry.getInitialInterval().toMillis());
		policy.setMultiplier(retry.getMultiplier());
		return policy;
	}

}
