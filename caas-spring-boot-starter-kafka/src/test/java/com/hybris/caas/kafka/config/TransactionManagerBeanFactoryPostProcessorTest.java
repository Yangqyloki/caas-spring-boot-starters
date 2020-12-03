package com.hybris.caas.kafka.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionManagerBeanFactoryPostProcessorTest
{
	@Mock
	private ConfigurableListableBeanFactory beanFactory;
	@Mock
	private BeanDefinition beanDefinition;
	private final TransactionManagerBeanFactoryPostProcessor beanFactoryPostProcessor = new TransactionManagerBeanFactoryPostProcessor();

	@Test
	public void should_mark_transactionManager_as_primary()
	{
		when(beanFactory.getBeanDefinition(CaasKafkaTransactionConfig.TX_MANAGER_BEAN_NAME)).thenReturn(beanDefinition);
		beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
		verify(beanDefinition).setPrimary(true);
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void should_fail_postProcess_no_bean_found()
	{
		when(beanFactory.getBeanDefinition(CaasKafkaTransactionConfig.TX_MANAGER_BEAN_NAME)).thenThrow(NoSuchBeanDefinitionException.class);
		beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
	}
}
