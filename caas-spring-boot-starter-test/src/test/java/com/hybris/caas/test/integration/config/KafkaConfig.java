package com.hybris.caas.test.integration.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Spring configuration to re-create kafka topics to remove kafka messages from previous executions
 */
@Configuration
public class KafkaConfig implements SmartInitializingSingleton
{
	public static final int DEFAULT_TIMEOUT_IN_SEC = 5;

	private static final Logger LOG = LoggerFactory.getLogger(KafkaConfig.class);

	private final KafkaProperties properties;

	public KafkaConfig(final KafkaProperties properties)
	{
		this.properties = properties;
	}

	/**
	 * Same as {@link KafkaAutoConfiguration#kafkaAdmin()} with {@link KafkaAdmin#autoCreate} set to {@code false}.
	 */
	@Bean
	public KafkaAdmin kafkaAdmin()
	{
		final KafkaAdmin kafkaAdmin = new KafkaAdmin(this.properties.buildAdminProperties());
		kafkaAdmin.setFatalIfBrokerNotAvailable(this.properties.getAdmin().isFailFast());

		// suppress auto creation of topics during context initialization
		kafkaAdmin.setAutoCreate(false);

		return kafkaAdmin;
	}

	@Override
	public void afterSingletonsInstantiated()
	{
		recreateTopics();
	}

	@SuppressWarnings("squid:S2142")
	private void recreateTopics()
	{
		final KafkaAdmin kafkaAdmin = kafkaAdmin();

		try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties()))
		{
			final ListTopicsResult listTopicsResult = adminClient.listTopics();
			final Set<String> topics = listTopicsResult.names().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);

			LOG.info("About to recreate topics: {}", topics);

			final DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(topics);
			deleteTopicsResult.all().get(DEFAULT_TIMEOUT_IN_SEC, TimeUnit.SECONDS);
		}
		catch (InterruptedException | TimeoutException | ExecutionException e)
		{
			throw new IllegalStateException(
					"Failed to delete topics, check kafka broker is up and running (sometimes kafka broker fails to start)", e);
		}

		// this will recreate the kafka topics
		kafkaAdmin.setAutoCreate(true);
		kafkaAdmin.afterSingletonsInstantiated();
	}

}
