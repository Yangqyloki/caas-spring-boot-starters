package com.hybris.caas.test.integration.config;

import com.hybris.caas.test.integration.util.FailureInducingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

/**
 * Configuration specifically added for the purpose of the starters integration testing.
 * These configuration will allow us to recreate test scenarios that would not normally happen during regular service operation.
 */
@EnableJpaRepositories(basePackages = "com.hybris.caas.test")
@Configuration
public class TestConfig implements AsyncConfigurer
{
	private static final Logger LOG = LoggerFactory.getLogger(TestConfig.class);

	/**
	 * Register filter to throw exception for a request at path {@code /fail}.
	 *
	 * @return filter registration bean
	 */
	@Bean
	public FilterRegistrationBean failureFilter()
	{
		LOG.info("CaaS Test - registered filter to throw exception at path '/fail'.");

		final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(new FailureInducingFilter());
		registrationBean.setOrder(0);
		return registrationBean;
	}

	@Override
	public Executor getAsyncExecutor()
	{
		return new SyncTaskExecutor();
	}

}
