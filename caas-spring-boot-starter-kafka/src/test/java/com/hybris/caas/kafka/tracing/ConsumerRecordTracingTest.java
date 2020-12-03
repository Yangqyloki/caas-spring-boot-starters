package com.hybris.caas.kafka.tracing;

import brave.Span;
import brave.Tracer;
import brave.kafka.clients.KafkaTracing;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConsumerRecordTracingTest
{
	@Mock
	private Tracer tracer;

	@Mock
	private KafkaTracing kafkaTracing;

	@Mock
	private Span span;

	@Mock
	private Tracer.SpanInScope spanInScope;

	@Mock
	private RecordMessageConverter messageConverter;

	private ConsumerRecord<?, ?> consumerRecord = buildConsumerRecord();
	private Type type = TypeUtils.wrap(Dummy.class).getType();
	private Message<Dummy> message = MessageBuilder.withPayload(new Dummy()).build();

	private ConsumerRecordTracing consumerRecordTracing;

	@BeforeEach
	public void setup()
	{
		consumerRecordTracing = new ConsumerRecordTracing(tracer, kafkaTracing, messageConverter);

		when(kafkaTracing.nextSpan(consumerRecord)).thenReturn(span);
		when(span.name(any())).thenReturn(span);
		when(span.start()).thenReturn(span);
		when(tracer.withSpanInScope(span)).thenReturn(spanInScope);
	}

	@Test
	public void should_trace_and_convert_consumer_record()
	{
		final var countDownLatch = new CountDownLatch(1);
		final var messageList = Collections.synchronizedList(new ArrayList<>());

		doReturn(message).when(messageConverter).toMessage(consumerRecord, null, null, type);

		consumerRecordTracing.traceAndConvert(consumerRecord, Dummy.class, dummyMessage -> {
			messageList.add(dummyMessage);
			return countDownLatch::countDown;
		}).run();

		Assertions.assertEquals(1, messageList.size());
		Assertions.assertEquals(message, messageList.get(0));
		Assertions.assertEquals(0, countDownLatch.getCount());

		verify(span).name(any());
		verify(span).start();
		verify(span).finish();
	}

	@Test
	public void should_propagate_exception_when_conversion_fails()
	{
		doThrow(NumberFormatException.class).when(messageConverter).toMessage(any(), any(), any(), any());

		Assertions.assertThrows(NumberFormatException.class, () -> {
			consumerRecordTracing.traceAndConvert(consumerRecord, Dummy.class, dummyMessage -> null).run();
		});

		verify(span).error(any());
		verify(span).finish();
	}

	private ConsumerRecord<?, ?> buildConsumerRecord()
	{
		return new ConsumerRecord<>("topic", 0, 0, "key", "{}");
	}

	public static final class Dummy
	{
		// empty
	}
}
