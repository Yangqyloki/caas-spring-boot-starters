package com.hybris.caas.test.integration.service;

import brave.Tracing;
import com.hybris.caas.test.integration.config.RabbitConfig;
import com.hybris.caas.test.integration.controller.TracingController;
import com.hybris.caas.test.integration.util.TracingAssertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;

public class TracingReceiver
{
	private static final Logger LOG = LoggerFactory.getLogger("test-logger");

	private final Tracing tracing;

	public TracingReceiver(final Tracing tracing)
	{
		this.tracing = tracing;
	}

	@RabbitListener(queues = RabbitConfig.queueName)
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue(value = RabbitConfig.queueName, durable = "false"),
	        exchange = @Exchange(value = RabbitConfig.topicExchangeName, ignoreDeclarationExceptions = "true"),
	        key = RabbitConfig.routingKey)
	  )
	public void receiveRabbitMessage(String message)
	{
		// If this is just a connection test, then ignore it.
		if (message.equals("connection-test"))
		{
			return;
		}

		LOG.info("In the RabbitMQ receiver.");
		assertAndEnqueue();
	}

	@KafkaListener(topics = "my-topic")
	public void receiveKafkaMessage(@Payload final Message<String> message)
	{
		LOG.info("In the Kafka receiver with message: " + message);
		assertAndEnqueue();
	}

	private void assertAndEnqueue()
	{
		TracingAssertions.assertUserTraceContext(tracing);
		TracingAssertions.assertSpanParentId(tracing);
		TracingController.queue.offer(MDC.getCopyOfContextMap());
	}
}
