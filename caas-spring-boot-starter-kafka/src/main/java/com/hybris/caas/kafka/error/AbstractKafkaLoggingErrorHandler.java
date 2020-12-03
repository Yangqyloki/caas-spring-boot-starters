package com.hybris.caas.kafka.error;

import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.ExceptionConverter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base class to handle exception thrown during kafka listener
 */
public class AbstractKafkaLoggingErrorHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractKafkaLoggingErrorHandler.class);
	protected final ExceptionConverter exceptionConverter;

	public AbstractKafkaLoggingErrorHandler(final ExceptionConverter exceptionConverter)
	{
		this.exceptionConverter = exceptionConverter;
	}

	protected <T> void handleException(final Exception thrownException, final T data, Function<T, String> toBuildMessageFunction)
	{
		try
		{
			if (Objects.isNull(data))
			{
				LOG.error("Failed to process the message. The message is null.", thrownException);
			}
			else
			{
				final String message = toBuildMessageFunction.apply(data);
				final Throwable exception = Objects.nonNull(thrownException.getCause()) ? thrownException.getCause() : thrownException;
				final ErrorMessage errorMessage = exceptionConverter.toErrorMessage(exception);
				LOG.error("Failed to process the message: {}, error details: {}", message, errorMessage);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error while processing: {}", ObjectUtils.nullSafeToString(data), e);
		}

	}

	protected String buildMessageFromConsumerRecords(final ConsumerRecords<?, ?> data)
	{
		final StringBuilder message = new StringBuilder();
		for (ConsumerRecord<?, ?> record : data)
		{
			message.append(buildMessageFromConsumerRecord(record));
		}
		return message.toString();
	}

	protected String buildMessageFromConsumerRecord(final ConsumerRecord record)
	{
		final StringBuilder message = new StringBuilder();
		final Map<String, String> headers = Arrays.stream(record.headers().toArray())
				.collect(Collectors.toMap(Header::key, header -> new String(header.value(), StandardCharsets.UTF_8)));

		message.append("topic: " + record.topic())
				.append(", headers: " + headers)
				.append(", payload: " + record.value())
				.append(System.lineSeparator());

		return message.toString();
	}
}
