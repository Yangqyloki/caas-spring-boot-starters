package com.hybris.caas.test.integration;

import com.hybris.caas.kafka.config.CaasKafkaProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;

public class KafkaStarterIntegrationTest extends AbstractIntegrationTest
{
	@Autowired
	private CaasKafkaProperties kafkaProperties;

	@Autowired
	private Set<NewTopic> newTopics;

	@Autowired
	private ConfigurableListableBeanFactory beanDefinitionRegistry;

	@Autowired
	private PlatformTransactionManager txManager;

	@Autowired
	@Qualifier("chainedKafkaTxManager")
	private PlatformTransactionManager chainedKafkaTxManager;

	@Test
	public void assert_CaasKafkaProperties()
	{
		assertThat(kafkaProperties.getEnvironmentName(), equalTo("test"));
		assertThat(kafkaProperties.getProducer().get("first").getDestination(), equalTo("my-topic"));
	}

	@Test
	public void should_create_NewTopic_beans_dynamically()
	{
		assertThat(newTopics, hasSize(1));

		final NewTopic topic = newTopics.iterator().next();
		assertThat(topic.name(), equalTo("my-topic"));
	}

	@Test
	public void should_create_ChainedKafkaTransactionManager()
	{
		assertTrue(txManager instanceof JpaTransactionManager);
		assertTrue(chainedKafkaTxManager instanceof ChainedKafkaTransactionManager);
	}

	@Test
	public void should_mark_transactionManager_beanDefinition_as_primary()
	{
		final BeanDefinition primaryTxManagerBeanDefinition = beanDefinitionRegistry.getBeanDefinition("transactionManager");
		assertTrue(primaryTxManagerBeanDefinition.isPrimary());
	}

}
