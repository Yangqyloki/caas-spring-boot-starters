package com.hybris.caas.kafka.tracing;

import brave.Span;
import brave.Tracer;
import brave.kafka.clients.KafkaTracing;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.messaging.Message;

import java.util.function.Function;

/**
 * Provides tracing capabilities for a {@link ConsumerRecord}.
 * When a Kafka listener receives one message at the time, tracing is built in by extracting the b3 related headers and other
 * Sleuth related propagation keys.
 * <p>
 * However, when a batch listener is being used, the tracing capabilities are not in place when an individual record from the
 * batch is being processed  by the service. This class targets exactly this scenario by enabling tracing for a particular
 * consumer record from the batch.
 */
public class ConsumerRecordTracing
{
	private Tracer tracer;
	private KafkaTracing kafkaTracing;
	private RecordMessageConverter messageConverter;

	public ConsumerRecordTracing(final Tracer tracer, final KafkaTracing kafkaTracing, final RecordMessageConverter messageConverter)
	{
		this.tracer = tracer;
		this.kafkaTracing = kafkaTracing;
		this.messageConverter = messageConverter;
	}

	/**
	 * Traces a consumer record by extracting the b3 related headers and other Sleuth related propagation keys similar to what
	 * is done by Sleuth message tracing code. Please see {@link org.springframework.cloud.sleuth.instrument.messaging.TraceMessagingAutoConfiguration}
	 * for more details.
	 * <p>
	 * The message processing code within a service does not work directly with {@link ConsumerRecord} instances but with {@link Message}
	 * instances. This method handles the conversion between {@link ConsumerRecord} and {@link Message} as well.
	 * <b>Important:</b> The conversion does not cover {@link org.springframework.kafka.support.Acknowledgment}
	 * and {@link org.apache.kafka.clients.consumer.Consumer} related headers.
	 *
	 * @param record          the consumer record
	 * @param clazz           the class type of consumer record payload
	 * @param functionToTrace the function handling the processing of the converter message
	 * @param <T>             the type of consumer record payload
	 * @return a {@link Runnable} handling the tracing of processing of the converter message
	 */
	@SuppressWarnings("squid:S1181")
	public <T> Runnable traceAndConvert(final ConsumerRecord<?, ?> record, final Class<T> clazz,
			final Function<Message<T>, Runnable> functionToTrace)
	{
		return () -> {
			final Span span = kafkaTracing.nextSpan(record).name("on-consumer-record").start();

			try (Tracer.SpanInScope spanInScope = tracer.withSpanInScope(span))
			{
				final Message<T> convertedMessage = (Message<T>) messageConverter.toMessage(record, null, null,
						TypeUtils.wrap(clazz).getType());

				functionToTrace.apply(convertedMessage).run();
			}
			catch (final RuntimeException | Error e)
			{
				span.error(e); // Unless you handle exceptions, you might not know the operation failed!
				throw e;
			}
			finally
			{
				span.finish();
			}
		};
	}
}
