package com.hybris.caas.kafka.config;

import com.hybris.caas.kafka.validator.ValidCaasKafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Validated
@ValidCaasKafkaProperties
@ConfigurationProperties(prefix = "caas.kafka")
public class CaasKafkaProperties
{
	/**
	 * The cloudfoundry kafka backing service name.
	 */
	@NotEmpty
	private String vcapServiceName;

	/**
	 * The cloudfoundry environment name (ie. test, stage or prod)
	 */
	@NotEmpty
	private String environmentName;

	/**
	 * The maximum amount of time the producer will wait for the response of a request (send producer record).
	 */
	@Positive
	private long producerSendTimeoutMs = 60000;

	/**
	 * Whether to create the consumer topics at startup if they don't exist in the kafka broker.
	 * Mostly used for local dev environments with clean kafka brokers.
	 * As in production environments the topics should have been previously created by the kafka producers.
	 */
	private boolean createConsumerTopics = false;

	/**
	 * Optional properties for a retry interceptor.
	 */
	private final Listener listener = new Listener();

	/**
	 * Optional properties for kafka batch listener
	 */
	private final Batch batch = new Batch();

	@NotNull
	@Valid
	@NestedConfigurationProperty
	private Map<String, Producer> producer = new HashMap<>();

	@NotNull
	@Valid
	@NestedConfigurationProperty
	private Map<String, Consumer> consumer = new HashMap<>();

	@NotNull
	@Valid
	@NestedConfigurationProperty
	private Map<String, RetryableConsumer> retryableConsumer = new HashMap<>();

	public String getVcapServiceName()
	{
		return vcapServiceName;
	}

	public void setVcapServiceName(final String vcapServiceName)
	{
		this.vcapServiceName = vcapServiceName;
	}

	public String getEnvironmentName()
	{
		return environmentName;
	}

	public void setEnvironmentName(final String environmentName)
	{
		this.environmentName = environmentName;
	}

	public long getProducerSendTimeoutMs()
	{
		return producerSendTimeoutMs;
	}

	public void setProducerSendTimeoutMs(final long producerSendTimeoutMs)
	{
		this.producerSendTimeoutMs = producerSendTimeoutMs;
	}

	public Listener getListener()
	{
		return listener;
	}

	public Batch getBatch()
	{
		return batch;
	}

	public Map<String, Producer> getProducer()
	{
		return producer;
	}

	public void setProducer(final Map<String, Producer> producer)
	{
		this.producer = producer;
	}

	public Map<String, Consumer> getConsumer()
	{
		return consumer;
	}

	public void setConsumer(final Map<String, Consumer> consumer)
	{
		this.consumer = consumer;
	}

	public Map<String, RetryableConsumer> getRetryableConsumer()
	{
		return retryableConsumer;
	}

	public void setRetryableConsumerMap(final Map<String, RetryableConsumer> retryableConsumer)
	{
		this.retryableConsumer = retryableConsumer;
	}

	public boolean isCreateConsumerTopics()
	{
		return createConsumerTopics;
	}

	public void setCreateConsumerTopics(final boolean createConsumerTopics)
	{
		this.createConsumerTopics = createConsumerTopics;
	}

	/**
	 * Producer topics
	 */
	public static class Producer
	{
		/**
		 * The number of partitions for the topic, if the topic exists and the {@code partitionCount} is different, it will be reconfigured.
		 */
		private int partitionCount = 1;

		/**
		 * The replication factor for the topic, if the topic exists and the {@code replicationFactor} is different, it will be reconfigured.
		 */
		private short replicationFactor = 1;

		/**
		 * The name of the topic to send kafka messages to.
		 */
		@NotNull
		private String destination;

		public int getPartitionCount()
		{
			return partitionCount;
		}

		public void setPartitionCount(final int partitionCount)
		{
			this.partitionCount = partitionCount;
		}

		public short getReplicationFactor()
		{
			return replicationFactor;
		}

		public void setReplicationFactor(final short replicationFactor)
		{
			this.replicationFactor = replicationFactor;
		}

		public String getDestination()
		{
			return destination;
		}

		public void setDestination(final String destination)
		{
			this.destination = destination;
		}

		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			final Producer producer = (Producer) o;
			return destination.equals(producer.destination);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(destination);
		}
	}

	/**
	 * Consumer and listener topics
	 */
	public static class Consumer
	{
		/**
		 * The name of the topic to consume from.
		 */
		@NotNull
		private String source;

		/**
		 * The number of partitions this topic has. Used to calculate the consumer container concurrency.
		 */
		@Min(1)
		private int partitionCount = 3;

		/**
		 * The replication factor for the topic, if the topic exists and the {@code replicationFactor} is different, it will be reconfigured.
		 */
		@Min(1)
		private short replicationFactor = 3;

		public String getSource()
		{
			return source;
		}

		public void setSource(final String source)
		{
			this.source = source;
		}

		public int getPartitionCount()
		{
			return partitionCount;
		}

		public void setPartitionCount(final int partitionCount)
		{
			this.partitionCount = partitionCount;
		}

		public short getReplicationFactor()
		{
			return replicationFactor;
		}

		public void setReplicationFactor(final short replicationFactor)
		{
			this.replicationFactor = replicationFactor;
		}
	}

	public interface Topic
	{
		int getPartitionCount();

		short getReplicationFactor();
	}

	public static class RetryableConsumer implements Topic
	{
		/**
		 * The name of the topic to consume from.
		 */
		@NotNull
		private String source;

		/**
		 * The number of partitions this topic has. Used to calculate the consumer container concurrency.
		 */
		@Min(1)
		private int partitionCount = 3;

		/**
		 * The replication factor for the topic, if the topic exists and the {@code replicationFactor} is different, it will be reconfigured.
		 */
		@Min(1)
		private short replicationFactor = 3;

		private RetryTopic shortDelayRetry;

		private RetryTopic longDelayRetry;

		private DeadLetterTopic deadLetter;

		public String getSource()
		{
			return source;
		}

		public void setSource(final String source)
		{
			this.source = source;
		}

		public int getPartitionCount()
		{
			return partitionCount;
		}

		public void setPartitionCount(final int partitionCount)
		{
			this.partitionCount = partitionCount;
		}

		public RetryTopic getShortDelayRetry()
		{
			return shortDelayRetry;
		}

		public void setShortDelayRetry(final RetryTopic shortDelayRetry)
		{
			this.shortDelayRetry = shortDelayRetry;
		}

		public RetryTopic getLongDelayRetry()
		{
			return longDelayRetry;
		}

		public void setLongDelayRetry(final RetryTopic longDelayRetry)
		{
			this.longDelayRetry = longDelayRetry;
		}

		public DeadLetterTopic getDeadLetter()
		{
			return deadLetter;
		}

		public void setDeadLetter(final DeadLetterTopic deadLetter)
		{
			this.deadLetter = deadLetter;
		}

		public short getReplicationFactor()
		{
			return replicationFactor;
		}

		public void setReplicationFactor(final short replicationFactor)
		{
			this.replicationFactor = replicationFactor;
		}

	}

	public static class RetryTopic implements Topic
	{
		/**
		 * The topic prefix name, if not provided the topic prefix will be the retryable-consumer source.
		 */
		private String topicPrefix;

		/**
		 * The number of partitions this topic has. Used to calculate the consumer container concurrency.
		 */
		@Min(1)
		private int partitionCount = 3;

		/**
		 * The replication factor for the topic, if the topic exists and the {@code replicationFactor} is different, it will be reconfigured.
		 */
		@Min(1)
		private short replicationFactor = 3;

		/**
		 * The minimum delay that should be enforced for the message before being processed.
		 */
		@Min(1)
		@Max(7_200_000)
		private int messageDelayMs = 60_000;

		public String getTopicPrefix()
		{
			return topicPrefix;
		}

		public void setTopicPrefix(final String topicPrefix)
		{
			this.topicPrefix = topicPrefix;
		}

		public int getPartitionCount()
		{
			return partitionCount;
		}

		public void setPartitionCount(final int partitionCount)
		{
			this.partitionCount = partitionCount;
		}

		public int getMessageDelayMs()
		{
			return messageDelayMs;
		}

		public void setMessageDelayMs(final int messageDelayMs)
		{
			this.messageDelayMs = messageDelayMs;
		}

		public short getReplicationFactor()
		{
			return replicationFactor;
		}

		public void setReplicationFactor(final short replicationFactor)
		{
			this.replicationFactor = replicationFactor;
		}
	}

	public static class DeadLetterTopic implements Topic
	{
		/**
		 * The topic prefix name, if not provided the topic prefix will be the retryable-consumer source.
		 */
		private String topicPrefix;

		/**
		 * The number of partitions this topic has.
		 */
		@Min(1)
		private int partitionCount = 1;

		/**
		 * The replication factor for the topic, if the topic exists and the {@code replicationFactor} is different, it will be reconfigured.
		 */
		@Min(1)
		private short replicationFactor = 3;

		public String getTopicPrefix()
		{
			return topicPrefix;
		}

		public void setTopicPrefix(final String topicPrefix)
		{
			this.topicPrefix = topicPrefix;
		}

		public int getPartitionCount()
		{
			return partitionCount;
		}

		public void setPartitionCount(final int partitionCount)
		{
			this.partitionCount = partitionCount;
		}

		public short getReplicationFactor()
		{
			return replicationFactor;
		}

		public void setReplicationFactor(final short replicationFactor)
		{
			this.replicationFactor = replicationFactor;
		}
	}

	public static class RetryableConsumerListener
	{
		/**
		 * Maximum number of attempts to deliver a message.
		 */
		private int maxAttempts = 2;

		/**
		 * Specify the container concurrency (number of consumers to create).
		 */
		@Min(3)
		private int concurrency = 9;

		/**
		 * Specify the container transaction identifier prefix. Must be the same for all instances of a service within a particular environment.
		 */
		private String transactionIdPrefix;

		public int getMaxAttempts()
		{
			return maxAttempts;
		}

		public void setMaxAttempts(final int maxAttempts)
		{
			this.maxAttempts = maxAttempts;
		}

		public int getConcurrency()
		{
			return concurrency;
		}

		public void setConcurrency(final int concurrency)
		{
			this.concurrency = concurrency;
		}

		public String getTransactionIdPrefix()
		{
			return transactionIdPrefix;
		}

		public void setTransactionIdPrefix(final String transactionIdPrefix)
		{
			this.transactionIdPrefix = transactionIdPrefix;
		}
	}

	public static class Listener
	{
		/**
		 * Optional properties for a retry interceptor.
		 */
		private final Retry retry = new Retry();

		private final RetryableConsumerListener retryableConsumer = new RetryableConsumerListener();

		public Retry getRetry()
		{
			return retry;
		}

		public RetryableConsumerListener getRetryableConsumer()
		{
			return retryableConsumer;
		}
	}

	public static class Retry
	{

		/**
		 * Whether publishing retries are enabled.
		 */
		private boolean enabled;

		/**
		 * Maximum number of attempts to deliver a message.
		 */
		private int maxAttempts = 2;

		/**
		 * Duration between the first and second attempt to deliver a message.
		 */
		private Duration initialInterval = Duration.ofMillis(250);

		/**
		 * Multiplier to apply to the previous retry interval.
		 */
		private double multiplier = 1.0;

		/**
		 * Maximum duration between attempts.
		 */
		private Duration maxInterval = Duration.ofMillis(3000);

		/**
		 * Whether exception cause should be traversed.
		 */
		private boolean traverseExceptionCauses;

		/**
		 * Indicates the default value when evaluating whether an un-configured exception should trigger a retry.
		 */
		private boolean retryExceptionDefaultValue;

		public boolean isEnabled()
		{
			return enabled;
		}

		public void setEnabled(final boolean enabled)
		{
			this.enabled = enabled;
		}

		public int getMaxAttempts()
		{
			return maxAttempts;
		}

		public void setMaxAttempts(final int maxAttempts)
		{
			this.maxAttempts = maxAttempts;
		}

		public Duration getInitialInterval()
		{
			return initialInterval;
		}

		public void setInitialInterval(final Duration initialInterval)
		{
			this.initialInterval = initialInterval;
		}

		public double getMultiplier()
		{
			return multiplier;
		}

		public void setMultiplier(final double multiplier)
		{
			this.multiplier = multiplier;
		}

		public Duration getMaxInterval()
		{
			return maxInterval;
		}

		public void setMaxInterval(final Duration maxInterval)
		{
			this.maxInterval = maxInterval;
		}

		public boolean isTraverseExceptionCauses()
		{
			return traverseExceptionCauses;
		}

		public void setTraverseExceptionCauses(final boolean traverseExceptionCauses)
		{
			this.traverseExceptionCauses = traverseExceptionCauses;
		}

		public boolean isRetryExceptionDefaultValue()
		{
			return retryExceptionDefaultValue;
		}

		public void setRetryExceptionDefaultValue(final boolean retryExceptionDefaultValue)
		{
			this.retryExceptionDefaultValue = retryExceptionDefaultValue;
		}
	}

	public static class Batch
	{
		/**
		 * Whether batch processing support should be enabled.
		 */
		private boolean enabled;
		/**
		 * Batch container factory concurrency.
		 */
		@Min(1)
		private int concurrency = 5;

		public boolean isEnabled()
		{
			return enabled;
		}

		public void setEnabled(final boolean enabled)
		{
			this.enabled = enabled;
		}

		public int getConcurrency()
		{
			return concurrency;
		}

		public void setConcurrency(final int concurrency)
		{
			this.concurrency = concurrency;
		}
	}
}
