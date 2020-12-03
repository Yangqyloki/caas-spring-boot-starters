package com.hybris.caas.kafka.config;

import com.hybris.caas.kafka.transaction.ChainedKafkaTransactionAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

@Configuration
@AutoConfigureAfter({ HibernateJpaAutoConfiguration.class, KafkaAutoConfiguration.class })
@ConditionalOnBean(name = CaasKafkaTransactionConfig.TX_MANAGER_BEAN_NAME, value = { KafkaTransactionManager.class })
public class CaasKafkaTransactionConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(CaasKafkaTransactionConfig.class);
	public static final String TX_MANAGER_BEAN_NAME = "transactionManager";

	/**
	 * All Methods requiring JPA & Kafka transaction synchronization require to name the transaction like
	 * {@code @Transactional("chainedKafkaTxManager")}.
	 * Otherwise, the transaction manager named "transactionManager" will be used and by default this is a {@code JpaTransactionManager}.
	 */
	@Bean
	@DependsOn(TX_MANAGER_BEAN_NAME)
	public ChainedKafkaTransactionManager chainedKafkaTxManager(
			@Qualifier(TX_MANAGER_BEAN_NAME) final JpaTransactionManager jpaTransactionManager,
			final KafkaTransactionManager kafkaTransactionManager)
	{
		LOG.info("CaaS Kafka - created chained Kafka/Jpa transaction manager with bean name 'chainedKafkaTxManager'");
		kafkaTransactionManager.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ALWAYS);
		return new ChainedKafkaTransactionManager(kafkaTransactionManager, jpaTransactionManager);
	}

	@Bean
	public static BeanFactoryPostProcessor transactionManagerBeanFactoryPostProcessor()
	{
		return new TransactionManagerBeanFactoryPostProcessor();
	}

	@Bean
	public ChainedKafkaTransactionAspect chainedKafkaTransactionAspect(final DefaultKafkaProducerFactory producerFactory)
	{
		return new ChainedKafkaTransactionAspect(producerFactory);
	}
}
