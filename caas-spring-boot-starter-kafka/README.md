# SAP Hybris - CaaS Spring Boot Starter for Kafka
---
Adds CaaS specific Kafka auto-configuration.

## Features
___
* Configure Kafka security.
* Create Kafka topics at application startup.
* Define `ChainedKafkaTransactionManager` for best efforts 1 phase commit consistency model.
* Configure logging error handler for kafka consumer & listener.
* Configurable properties for retry template for kafka consumer & listener.
  * Configurable `RecoveryCallback<?>` to be called when all retries are exhausted to try to recover. 
* Configure retryable consumer that uses retry and dead letter topics.
* Configure batch consumer with the number of consumers and the timeout in ms
___

### Enabling CaaS Kafka
In order to enable the CaaS Kafka, you simply need to add the `com.hybris.caas:caas-spring-boot-starter-kafka` dependency to your build. All configurations will be done automatically via the spring auto-configuration process.


### Kafka Security Configuration
The SAP cloud platform Kafka clusters require the use of OAuth 2 access tokens for security purposes. 
This starter will provide the default properties.

```yaml
caas.kafka.vcap-service-name: caas2-kafka-intelligence
spring:
  kafka:
    bootstrap-servers: ${vcap.services.caas2-kafka-intelligence.credentials.cluster.brokers}
    security.protocol: SASL_SSL
    properties:
      sasl.mechanism: PLAIN
    jaas:
      login-module: org.apache.kafka.common.security.plain.PlainLoginModule
      control-flag: required
      options:
        username: ${caas.kafka.jaas.username}
        password: ${caas.kafka.jaas.password}
      enabled: true
    ssl.trust-store-location: ${caas.kafka.ssl.trust-store-location}
    ssl.trust-store-password: ${caas.kafka.ssl.trust-store-password}
```

The CA root certificate it's downloaded from `${vcap.services.${caas.kafka.vcap-service-name}.credentials.urls.ca_cert}`.  
And a temporary ssl trust store is created with a random password.  
The location and password are provided as the `caas.kafka.ssl.trust-store-location` and `caas.kafka.ssl.trust-store-location` properties.

The `caas.kafka.jaas.username` is the same as `${vcap.services.${caas.kafka.vcap-service-name}.credentials.username}`.  
The `caas.kafka.jaas.password` is obtained by getting an OAuth2 access token from `${vcap.services.${caas.kafka.vcap-service-name}.credentials.urls.token}`.  

For more details please refer to the class `com.hybris.caas.kafka.config.CaasKafkaEnvPostProcessor` 
and properties file `src/main/resources/kafka.properties`.

Currently, the default Cloud Foundry service name for the Kafka backing service is `caas2-kafka-intelligence`. If this changes, or you want to use this starter against a different Kafka backing service,
Then simply set the name of your backing service by setting this property:
`caas.kafka.vcap-service-name=<name of Kafka backing service in Cloud Foundry>`


### Create Topics
In order to have your topics created automatically at application startup, you will have to configure your topics via properties.
You can also specify the topic's number of partitions and replication factor. Each topic is defined under a single producer binding in the properties.
Also, the producer binding name is arbitrary and unrelated to the topic name; the latter is defined in the producer binding's `destination` property.

```yaml
caas.kafka.producer:
  first:
    partition-count: 2
    replication-factor: 1
    destination: my-first-topic   # topic name
  second:
    partition-count: 3
    replication-factor: 3
    destination: my-second-topic  # topic name
```

Since CaaS is currently sharing a single Kafka cluster across all Cloud Foundry environments, it is required to specify a topic suffix.
This suffix will allow us to namespace the topics for each environment. Also, the shared cluster currently has 3 partitions and 3 replicas,
so there are the ideal settings for the producer binding `partition-count` and `replication-factor` respectively for your cloud profiles.

A configuration property, `environment-name`, is available for helping with the topic suffix as well as for other cases that requires us to namespace configuration properties per environment.
It is suggested to use Spring profiles to specify the topic suffix as per example below in your `application.yml`.
```yaml

---
spring.profiles: dev
caas.kafka:
  environment-name: dev
  producer:
    product-edition-added:
      destination: product-edition-added-${caas.kafka.environment-name}

---
spring.profiles: stage
caas.kafka:
  environment-name: stage
  producer:
    product-edition-added:
      destination: product-edition-added-${caas.kafka.environment-name}

---
spring.profiles: prod
caas.kafka:
  environment-name: prod
  producer:
    product-edition-added:
      destination: product-edition-added-${caas.kafka.environment-name}

```

By default spring-kafka creates the producer topics if they don't exist.
For the consumer topics, the following property can be set to true
```yaml
caas.kafka.create-consumer-topics: true
```
### Kafka consumer factory

A consumer factory bean, `kafkaConsumerFactory`, is provided with the following configuration in place:
* Round robin assignor - round robin assignment for partitions to consumers
* Disable offset auto commit

*Important:* Please note that the default Kafka consumer as well as the Retryable Kafka consumer described below rely on the configuration
mentioned above being in place for the consumer factory bean.

### Kafka consumer / listener

Compared to the producer side, the consumer won't attempt to create new topics or reconfigure existing topics with partition count or
replication factor by default. Although, this can be be done by setting `caas.kafka.create-consumer-topics: true`.

The partition count will be used to calculate the container concurrency which is the number of consumers to create.  
Along with the property set on the consumer, round robin assignor, the partitions will be distributed across all consumers.
Please note that each instance of the service will get the same number of consumers created (equal to the concurrency value).
At this point, the consumers are not distributed among all the service instances; they are all created per each service instance.

*Important:*
Since consumers could be creating the topics as well, it is important that both the consumer and producer set the same values
for `partition-count` and `replication-count` properties. The minimum replication factor for a high availability deployment is 3 (default value).
The number of partitions could be increased later on but not decrease.
Also, only new data gets distributed based on the hash across all partitions but the data from the existing partitions is not redistributed.
Consumer re-balancing is also performed after partitions are being added.
Please see [kafka documentaion](https://kafka.apache.org/documentation/#basic_ops_modify_topic) for more details.  

```yaml
caas.kafka
  consumer:
    first:
      source: consumer-topic-1   # topic name
      partition-count: 2 # default 3, used to calculate container concurrency.
      replication-factor: 3 # default 3, if the topic exists and the replication factor is different, it will be reconfigured.
    second:
      source: consumer-topic-1  
      partition-count: 3
  listener.retry:
    enabled: true # Whether consuming retries are enabled.
    initial-interval: 1000ms # Duration between the first and second attempt to deliver a message.
    max-attempts: 3 # Maximum number of attempts to deliver a message.
    max-interval: 10000ms # Maximum duration between attempts.
    multiplier: 1 # Multiplier to apply to the previous retry interval.
```

Listener managed transactions pattern is being used for this default Kafka consumer. The transaction manager for the container it is set to `null`.

The consumer can be configure for retrying consuming messages, stateless retry. That is, it will not acknowledge the reception of a message until it
retries `max-attempts` times to process the message. If the listener method is annotated with `@Transactional`, a JPA/DB transaction is started
for each invocation of the listener method.  
After the number of attempts are reached:
* if no `recoveryCallback` is configured, the message will be ack'd and the `errorHandler` will be executed.
* if a `recoveryCallback` is configured, it will be called after all attempts from the `retryTemplate` has been exhausted.  
  If no exception thrown within the `recoveryCallback`, meaning a successful recovery, the message is ack'd and the kafka consumer continues processing messages.  
  If an exception is thrown during the `recoveryCallback`, unable to recover, the message will be ack'd and the `errorHandler` will be executed.

In order to configure a `recoveryCllback`, enable retry template via `caas.kafka.listener.retry: true` and define a bean that extends `org.springframework.retry.RecoveryCallback`
```java
    @Bean
    public RecoveryCallback<?> recoveryCallback() {
        return context -> { ... } 
    }
```

A default error handler is provided `LoggingErrorHandler` that logs the record at `ERROR` level.

Other error handlers could be used instead of the default one such as `DeadLetterPublishingErrorHandler` which publishes the message
that failed processing into a dead-letter topic. The dead-letter topic name will be derived from the consumer topic name plus the
suffix `.DLT`, for example for the topic name `topic-to-consume-from` the dead-letter topic name will be `topic-to-consume-from.DLT`.
The `DeadLetterPublishingErrorHandler` will enhance the headers to keep all the information about the original `ConsumerRecord`,
its headers and the exception. These added headers start with the prefix `kafka_dlt-`. For more information about `DeadLetterPublishingErrorHandler`,
please see [`Publishing Dead-letter Records`](https://docs.spring.io/spring-kafka/reference/html/#dead-letters) section from spring-kafka documentation.

The consumer is configured to disable async auto commit and the container is configured with ack mode set to `RECORD`
(ack after each record has been passed to the listener).  
Spring kafka will infer the generic type from the `Message<?>` and convert the message, given it's configured with `StringJsonMessageConverter`. If the conversion fails, the `errorHandler` will be executed.

```java
	@Transactional
	@KafkaListener(topics = "${caas.kafka.consumer.first.source}")
	public void receiveMessage(@Payload final Message<MyDTO> message,
			@Header(KafkaHeaders.RECEIVED_TOPIC) final String topic, @Header(KafkaHeaders.OFFSET) final Long offset)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("Received kafka message, topic: {}, offset: {}, message: {}", topic, offset, message);
		}

		// process the message
	}
```

### Retryable Kafka consumer / listener

A retryable consumer is also available. This is different than the standard Kafka consumer and has a specific
built in behavior for handling and retrying messages that failed processing.

*Pre-requisite:*
* `spring.kafka.producer.transaction-id-prefix` needs to be set in order to enable transactions and use the retryable consumer.
* `caas.kafka.listener.retryable-consumer.transaction-id-prefix` needs to be set in order to enable transactions and use the retryable consumer.
Please see `Namespacing Configuration Properties` section of this document on how to set these properties.

Besides that built in behavior it requires additional listener configuration on the service side.

The retryable consumer covers the following flow when full configuration is provided:
1) Message is being consumed from regular topic and in case of failure it is published to a short delay retry topic.
2) Message is being consumed from short delay retry topic after enforcing the required delay and in case of failure it is published
to a long delay retry topic.
3) Message is being consumed from the long delay retry topic after enforcing the required delay and in case of failure it is published
to a dead letter topic.

It is not require to configure a retry and dead letter topics for a retryable consumer. If any such configuration is not present,
the corresponding retry or dead letter topic will be skipped. For example, if `short-delay-retry` configuration is missing, the failing message
once consumed from the source topic it will be published to `long-delay-retry` topic and then to `dead-letter` topic if still failing processing.
If the `dead-letter` configuration is missing, then the message will be logged and no publishing to dead letter topic takes place.

At service start-up, the retryable consumer will attempt to create the configured topics, or reconfigure the existing topics with partition count or replication factor.

In order to achieve this flow, multiple Kafka listeners need to be defined on the service side that perform the same business processing for the message.
The built in behavior is the selection of the retry or dead letter topic based on the failed's message current topic as well as enforcing
any required delay before the processing of the message as configured for the delay retry topic using `message-delay-ms` property.
The selection of the retry topic or the dead letter topic is done by inspecting the suffix of the topic from which the failed message was consumed.

The listener container supporting the retryable consumer, `retryableKafkaListenerContainerFactory`, needs to be used when configuring the listeners.

The processing of a message for the retryable consumer would be done in a Kafka container managed transaction and this means that
the handling of the message, the eventual publishing to a retry topic or dead letter topic (done using a custom `ErrorHandler` implementation),
and the ack of the message would be done in the same Kafka transaction.
However, the handling of the messages would be done into a separate DB transaction started by Spring according to `@Transaction`
annotation that may be applied to the listener method.

In case the publishing to a retry topic or dead letter topic fails, the container managed transaction is being rolled back and the consumer
is being repositioned in order for the message to be re-delivered again by the next poll. If this error situation persists, the message
is being retried 2 more times, when using the default configuration for `retryable-consumer.max-attempts`, before being skipped and an
`ERROR` level entry being logged for the message. This is achieved by leveraging `DefaultAfterRollbackProcessor` behind the scene.

The enforcement of the delay before the retry is being done by using a `RecordInterceptor` and throwing `DelayException` if the message
timestamp plus the configured delay for the delayed retry topic has not yet elapsed. The `DelayException` is propagated and not
handled by the custom `AfterRollbackProcessor` that leverages the `DefaultAfterRollbackProcessor` in order for the container managed
transaction to be rolled back. Also, when `DelayException` is being thrown, the consumer is being repositioned by using `SeekUtils.doSeeks`
in order for the message to be re-delivered again by the next poll.

At this point, the container concurrency is another configurable parameter, `concurrency`, that is the number of consumers to create.  
Along with the property set on the consumer `partition.assignment.strategy=org.apache.kafka.clients.consumer.RoundRobinAssignor` it
will distribute the partitions across all of the consumers.

```yaml
caas.kafka:
  retryable-consumer:
    <consumer-name>:                                        # example: product-fulfillment-location
      partition-count: 3                                    # default 3
      replication-factor: 3                                 # default 3
      source: <topic-name>-${caas.kafka.environment-name}   # example: inventory-product-fulfillment-location-updated-${caas.kafka.environment-name}
      short-delay-retry:                                    # optional, configuration for short delay retry topic. No short delay retry topic, if not provided.
        topicPrefix: <short-retry-topic>                    # optional, if provided a '.SDR' suffix will be added, otherwise the '${retryable-consumer.source}.SDR' will be used
        partition-count: 3                                  # default 3                           
        replication-factor: 3                               # default 3
        message-delay-ms: 15000                             # minimum delay to be enforced before consuming the message.
      long-delay-retry:                                     # optional, configuration for long delay retry topic. No long delay retry topic, if not provided.
        topicPrefix: <long-retry-topic>                     # optional, if provided a '.LDR' suffix will be added, otherwise the '${retryable-consumer.source}.LDR' will be used
        partition-count: 3                                  # default 3
        replication-factor: 3                               # default 3
        message-delay-ms: 30000                             # minimum delay to be enforced before consuming the message.
      dead-letter:                                          #(optional) configuration for dead letter topic. No dead letter topic, if not provided.
        topicPrefix: <dead-letter-topic>                    # optional, if provided a '.DLT' suffix will be added, otherwise the '${retryable-consumer.source}.DLT' will be used
        partition-count: 1                                  # default 1
        replication-factor: 1                               # default 3
  listener:
    retryable-consumer:
      max-attempts: 2                                                         # default 2, the maximum no of attempts to process a message in case publishing to retry/dead letter topic fails before logging the message.
      concurrency: 9                                                          # default 9, the concurrency for the retryable consumer listener container.
      transaction-id-prefix: <service-name>-tx-${caas.kafka.environment-name}-   # specifies the transaction identifier prefix that should be used by the container; should be different per environment but the same across service instances.
```

The topic suffixes that are being used for the retry and dead letter topics are: `.SDR`, `.LDR`, and `.DLT`.
Please see `CaasKafkaConstants` for the constants defined for these suffixes.

The topic names of the retry and dead letter topics can be configured via the property `topicPrefix` and their respective suffix will be added.
Otherwise, if no `topicPrefix` configured, the retryable-consumer's source will be used as `topicPrefix`.

`DeadLetterPublishingErrorHandler` is being leveraged behind the scene to publish the failed message to the retry or dead-letter topic.
`DeadLetterPublishingErrorHandler` will enhance the headers to keep all the information about the original `ConsumerRecord`, its headers and the exception.
These headers start with the prefix `kafka_dlt-`. However, when the message is being published to a retry topic only a subset of these headers are preserved;
for example, the header storing the stacktrace is removed (please see `RetryableConsumerDeadLetterPublishingRecoverer` for more details).

When the message gets published to the retry or dead letter topic, the message's timestamp gets overridden with the current timestamp
and this new timestamp is being used to enforce the delay for a retry topic. The message's original timestamp is being preserved in a
`kafka_dlt-` prefixed header. Also, the partition is being set to `-1`, a negative value, to allow Kafka to select the partition for the
retry or dead letter topic.

*Important:*
Kafka broker's default configuration as well as the topic's default configuration for `log.message.timestamp.type` is `CreateTime`.
This means that the producer should set the timestamp of the record. In the case when `log.message.timestamp.type` is set to `LogAppendTime`
the broker will overwrite the timestamp type and the timestamp in the message.

The retryable consumer is configured to disable async auto commit and use `RECORD` acknowledgment.  
Spring kafka will infer the generic type from the `Message<?>` and convert the message, given it's configured with `StringJsonMessageConverter`.
If the conversion fails, the `errorHandler` will be executed. Please note that certain exceptions, such as `MessageConversionException`
do not cause a message to be retried 3 times in case publishing to retry or dead letter topic fails.
For such exceptions the message will only be consumed once. Please see `After-rollback Processor` documentation for the list of these type of exceptions.

As an insight note, the `transactionIdPrefix` and the producer factory must be the same for the `KafkaTemplate` and `KafkaTransactionManager`
used by the retryable consumer. This is taken care by the auto-configuration put in place in `CaasKafkaConfig` but `transactionIdPrefix`
should be configured for the retryable consumer. An additional suffix `container-` will be added to the configured `transactionIdPrefix`
before setting it on `KafkaTemplate` and `KafkaTransactionManager` used by the retryable consumer.

*Important:* Please note that a message might be retried more than once per a retry topic; the exactly once semantics are not supported.
This could happen due to consumers rebalancing in situations such as when polled records are not processed within the `max.poll.interval.ms`
interval or at service re-start time when consumers are being stopped/started.

*Consumer Considerations:* The consumer should use `isolation.level: read_committed` when processing messages.
The messages are being published in a transaction and in the case when the transaction rolls back, a published message could be available
in Kafka but not committed. It is important for the consumer to skip these not committed Kafka messages.

*Producer Considerations:* The producer being configured in the same service with a retryable consumer should be transactional.
This is achieved by setting the `transactionIdPrefix` configuration property for the producer. If this is not the case or not desirable
an error is reported and a separate non-transactional producer factory needs to be created by the service to support such use case.
Also. the producer needs to set the timestamp for the published messages.

```yaml
spring:
  kafka:
    client-id: <service-name>-${caas.kafka.environment-name}
    producer:
      transaction-id-prefix: <service-name>-tx-${caas.kafka.environment-name}-${random.uuid}-
    consumer:
      client-id: <service-name>-consumer-${caas.kafka.environment-name}
      group-id: <service-name>-kafka-group-${caas.kafka.environment-name}
      properties:
        isolation.level: read_committed
```

```java
import static com.hybris.caas.kafka.util.CaasKafkaConstants.LONG_DELAY_RETRY_TOPIC_SUFFIX;
import static com.hybris.caas.kafka.util.CaasKafkaConstants.SHORT_DELAY_RETRY_TOPIC_SUFFIX;

@Component
public class ProductFulfillmentLocationKafkaListener
{
	@Transactional
	@KafkaListener(topics = "${caas.kafka.retryable-consumer.product-fulfillment-location.source}",
	containerFactory = "retryableKafkaListenerContainerFactory")
	public void receiveMessage(@Payload final Message<ProductFulfillmentLocation> message,
			@Header(TENANT) final String tenantFromHeader)
	{
		LOG.debug("Executing ProductFulfillmentLocationKafkaListener with message for product {}.", message.getPayload().getProductId());
      ...
	}

	@Transactional
	// SPEL expression to concatenate configurable topic name and suffix constant
	@KafkaListener(topics = "#{'${caas.kafka.retryable-consumer.product-fulfillment-location.source}' + " + "'"
			+ SHORT_DELAY_RETRY_TOPIC_SUFFIX + "'}", containerFactory = "retryableKafkaListenerContainerFactory")
	public void receiveShortRetryMessage(@Payload final Message<ProductFulfillmentLocation> message,
			@Header(TENANT) final String tenantFromHeader)
	{
		LOG.debug("Short-retry - Executing ProductFulfillmentLocationKafkaListener with message for product {}.",
				message.getPayload().getProductId());
      ...
	}

	@Transactional
	// SPEL expression to concatenate configurable topic name and suffix constant
	@KafkaListener(topics = "#{'${caas.kafka.retryable-consumer.product-fulfillment-location.source}' + " + "'"
			+ LONG_DELAY_RETRY_TOPIC_SUFFIX + "'}", containerFactory = "retryableKafkaListenerContainerFactory")
	public void receiveLongRetryMessage(@Payload final Message<ProductFulfillmentLocation> message,
			@Header(TENANT) final String tenantFromHeader)
	{
		LOG.debug("Long-retry - Executing ProductFulfillmentLocationKafkaListener with message for product {}.",
				message.getPayload().getProductId());
		...
	}
}
```

### Stateless retries consumer / listener

The consumer can be configured for stateless retries of consuming messages. This means that a message will be retried `retry.max-attempts`
times depending on the retry exceptions configuration in case an exception is thrown during processing.
After the number of attempts are reached, the message will be provided to `errorHandler` for further handling according to
the type of the consumer being used (i.e. Retriable Kafka consumer or not).

The following example shows how stateless retries could be enabled:
```yaml
caas.kafka:
  listener:
    retry:
      enabled: true                       # default false, enables stateless retries
      initial-interval: 3000ms            # default 250ms, duration between the first and second retry attempt
      max-attempts: 3                     # default 2, maximum number of retry attempts for the message
      max-interval: 10000ms               # default 3000ms, maximum duration between retry attempts
      multiplier: 1                       # default 1, multiplier to apply to the previous retry interval
      traverse-exception-causes: true     # default false, enable exception cause traversing
      retry-exception-default-value: true # default false, indicates whether an un-configured exception should be retried
```

*Important:* In the case of the default Kafka consumer/listener container and retryable consumer, if the listener method is annotated with `@Transactional`,
a new JPA/DB transaction is created for each retry attempt.

A default exceptions map configurations is defined in the starters and it could be overridden by the service as needed by defining a
bean with `kafkaListenerRetryExceptionsMap` name and the appropriate `Map<Class<? extends Throwable>, Boolean>` type.

Here is the default configuration:
```java
@Bean
@ConditionalOnMissingBean(name = "kafkaListenerRetryExceptionsMap")
public Map<Class<? extends Throwable>, Boolean> kafkaListenerRetryExceptionsMap()
{
  return Collections.singletonMap(Exception.class, true);
}
```

For more details on how stateless retries work please see spring-kafka documentation and spring-retry project documentation
for details about the retry related components.  


### Batch Kafka consumer / listener

The consumer can be configured for batch consuming messages. A container for it is not created by default but it could be done by setting:
```application.properties
caas.kafka.batch.enabled=true
```
The error handler configured for the batch container is `BatchLoggingErrorHandler` which logs the batch messages at `ERROR` level.

The only configuration property supported at the moment for the batch container is `concurrency`, the number of consumers to create.

The default consumer factory is used for configuring the batch container as well (which has async auto commit disabled) and the ack mode configured for the container is `BATCH`.
Given the default ack on error being set to true, each batch of messages is acknowledge after consumption.

The following example shows how to enable and override the default configuration in the application.yaml:
```yaml
caas.kafka:
  batch:
    enabled: true
    concurrency: 3  # default 5, number of consumers to create.
```

Here is how the batch container bean is defined:
```java
@Bean
@ConditionalOnMissingBean(name = "kafkaBatchListenerContainerFactory")
@ConditionalOnProperty(name = "caas.kafka.batch.enabled", havingValue = "true")
public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaBatchListenerContainerFactory (
... 
```  

Here is how the batch container could be used for configuring a listener:
```java
@KafkaListener(topics = "${caas.kafka.consumer.order-created.source}", containerFactory = "kafkaBatchListenerContainerFactory")
public void handleOrderCreated(final List<Message<OrderEventPayload>> messages)
{
    executeInParallel(messages);
}
```

Tracing capabilities provided by Sleuth through the use of b3 headers and propagation keys are lost when batch listener is used.
However, all the necessary information for tracing a message is available in the headers of the message and support for using
and applying it is available but based on `ConsumerRecord` class instead of the `Message` class.

`ConsumerRecordTracing` bean is provided in the starters along with the batch listener to extract and set the tracing context when
a particular Kafka message from the batch of messages is processed. Please see more details in the Javadoc for `ConsumerRecordTracing`.

In order to use it the definition of the listener needs to be changed to receive a list of `ConsumerRecord`s as indicated below:
```java
@KafkaListener(topics = "${caas.kafka.consumer.order-created.source}", containerFactory = "kafkaBatchListenerContainerFactory")
public void handleOrderCreated(final List<ConsumerRecord<?, ?>> messages)
{
    executeInParallel(messages);
}
```

### ChainedKafkaTransactionManager
The Spring `ChainedKafkaTransactionManager` allows you to chain multiple `PlatformTransactionManager`s, where at least one of those is the `KafkaTransactionManager`.
This will allow you to begin and commit transactions by following a chain of responsibility. This is very useful for achieving what is known as a
"Best Efforts 1 phase commit".

To use the `ChainedKafkaTransactionManager`, you can specify it in a declarative transactional annotation, the ChainedKafkaTransactional annotation or inject it and use it programmatically.

**Declarative Approach**
```java
@Transactional("chainedKafkaTxManager")
```

**ChainedKafkaTransactional Approach**
```java
@ChainedKafkaTransactional
```

**Programmatical Approach**
```java
@Qualifer("chainedKafkaTxManager")
@Autowired
private PlatformTransactionManager chainedKafkaTxManager;
```

The `ChainedKafkaTransactionManager` auto-configuration will only be applied if both the `KafkaTransactionManager` bean the `JpaTransactionManager` bean named `transactionManager` both exist
and the `spring.kafka.producer.transaction-id-prefix` property has been defined.
This auto-configuration will also promote the `transactionManager` bean definition to be the **primary** `PlatformTransactionManager`.
This will help ensure that the injection of the default `JpaTransactionManager` can occur without a qualifier.

**Note**: When producing messages in a Kafka transaction, the consumer must be aware and needs to set its isolation level to `READ_COMMITTED`.
With Spring-Kafka, this is done by setting the property: `spring.kafka.consumer.properties.isolation.level=read_committed`.

### SyncKafkaTemplate

The `SyncKafkaTemplate` bean provided handles sending kafka messages in a synchronous fashion,
and wraps any exception resulting from sending a message into a `KafkaRuntimeException`

### ChainedKafkaTransactionAspect

The `ChainedKafkaTransactionAspect` executes when an `KafkaRuntimeException` and after the transaction has been completed,  
in order to close any kafka producers following a un-recoverable kafka exception.<br>
This is due to the fact that un-recoverable kafka producers will throw exceptions if continue to be used as they kept out of date metadata
about the topics. And new producers need to be instantiated with up to date metadata

### Namespacing Configuration Properties
Since CaaS is currently sharing a single Kafka cluster across all Cloud Foundry environments, it is required to namespace different configuration properties.

So far, the following configuration properties have been identified to require namespacing:
* Topics - please see Create Topic section for more details.
* Client Identifiers - the suggestion is to use the environment name as suffix when naming these identifiers.
* Transactional Identifier:
  * Producer - the suggestion is to use the environment name as well as an unique random value part of the identifier in order to avoid ProducerFencedException (thrown when another producer with the same identifier is active).
  * Retryable consumer - the suggestion is to use the environment name part of the identifier to satisfy [fencing zombies](https://www.confluent.io/blog/transactions-apache-kafka/).

Also, in order to avoid name clashing it is suggested to use name of the service as prefix when naming these identifiers. Please see the example below.

```yaml
spring:
  kafka:
      client-id: product-content-${caas.kafka.environment-name}
    producer:
      client-id: product-content-producer-${caas.kafka.environment-name}
      transaction-id-prefix: product-content-tx-${caas.kafka.environment-name}-${random.uuid}-  # <service-name>-tx-${caas.kafka.environment-name}-${random.uuid}-
    consumer:
      client-id: product-content-consumer-${caas.kafka.environment-name}
caas.kafka:
  listener:
    retryable-consumer:
      transaction-id-prefix: product-content-tx-${caas.kafka.environment-name}-   # <service-name>-tx-${caas.kafka.environment-name}-
```

### MessageAssembler
Assembles a message with default headers(TOPIC, TENANT, TIMESTAMP, MESSAGE_KEY)
```java
final Message<String> message = messageAssembler.assemble(KEY, TENANT_ID, PAYLOAD);
```
Assembles a message with custom headers = Map<String, Object>
the custom header map can't have an override value for KEY nor TENANT
```java
final Message<String> message = messageAssembler.assemble(KEY, TENANT_ID, PAYLOAD, headers);
```
### transactionIdPrefix
Please see the `transactionIdPrefix` section from spring-kafka documentation for more details.

As a short summary, in the context of multi-instance app when publishing messages on a listener container thread which is the case
of retryable consumer, the value of `transactionIdPrefix` must be the same on all instances in order to satisfy fencing zombies.
However, when publishing messages using transactions not started by a listener container, the value of `transactionIdPrefix` must be
different on each app instance.  
