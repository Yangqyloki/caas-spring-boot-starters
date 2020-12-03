package com.hybris.caas.kafka.util;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Utility methods for dealing with {@link ConsumerRecord}.
 */
public final class ConsumerRecordUtils
{
	private ConsumerRecordUtils()
	{
		// nothing to do here
	}

	/**
	 * Gets the value of the last occurrence of the specified header.
	 *
	 * @param record     the consumer record to get the value of the header from
	 * @param headerName the name of the header
	 * @return a {@link String} containing the value of the specified header or null if the header is not found
	 */
	public static String getLastConsumerRecordHeader(final ConsumerRecord<?, ?> record, final String headerName)
	{
		return Optional.ofNullable(record.headers().lastHeader(headerName))
				.map(header -> new String(header.value(), StandardCharsets.UTF_8))
				.orElse(null);
	}
}
