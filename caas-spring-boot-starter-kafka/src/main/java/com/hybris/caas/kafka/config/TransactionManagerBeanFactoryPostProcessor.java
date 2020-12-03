package com.hybris.caas.kafka.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Bean factory post processor to modify the bean definition for the bean with name 'transactionManager' by setting it's primary flag.
 */
public class TransactionManagerBeanFactoryPostProcessor implements BeanFactoryPostProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(TransactionManagerBeanFactoryPostProcessor.class);

	@Override
	public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory)
	{
		// Dynamically register the default 'transactionManager' as the primary transaction bean.
		final BeanDefinition primaryTxManagerBeanDefinition = beanFactory.getBeanDefinition(CaasKafkaTransactionConfig.TX_MANAGER_BEAN_NAME);
		primaryTxManagerBeanDefinition.setPrimary(true);
		LOG.info("CaaS Kafka - bean 'transactionManager' found and has been marked as 'primary'");
	}
}
