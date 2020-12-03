package com.hybris.caas.test.integration.config;

import brave.Tracing;
import com.hybris.caas.test.integration.service.TracingReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableAsync
@Configuration
public class TracingConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(TracingConfig.class);

	@Bean
	public ExecutorService executorService()
	{
		return Executors.newSingleThreadExecutor();
	}

	@Bean
	public TraceableExecutorService traceableExecutorService(BeanFactory beanFactory, ExecutorService executorService)
	{
		return new TraceableExecutorService(beanFactory, executorService, "traceableExecutorSpan");
	}

	@Bean
	public TracingReceiver testReceiver(final Tracing tracing)
	{
		LOG.info("CaaS Test - registered test message receiver for RabbitMQ and Kafka.");
		return new TracingReceiver(tracing);
	}

}