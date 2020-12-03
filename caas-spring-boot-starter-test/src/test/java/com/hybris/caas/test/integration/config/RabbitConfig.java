package com.hybris.caas.test.integration.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig
{
	public static final String topicExchangeName = "spring-boot-exchange";
	public static final String queueName = "spring-boot";
	public static final String routingKey = "test-key";

	@Bean
	public Queue queue()
	{
		return new Queue(queueName, false);
	}

	@Bean
	public TopicExchange exchange()
	{
		return new TopicExchange(topicExchangeName);
	}

	@Bean
	public Binding binding(Queue queue, TopicExchange exchange)
	{
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);
	}

}
