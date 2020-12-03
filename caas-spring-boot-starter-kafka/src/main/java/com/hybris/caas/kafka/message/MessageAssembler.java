package com.hybris.caas.kafka.message;

import org.springframework.messaging.Message;

import java.util.Locale;
import java.util.Map;

/**
 * Defines methods for assembling a {@link Message} of type {@code S} based on the source object of type {@code S} to be sent to Kafka.
 *
 * @param <S> the type of the message and source object
 */
public interface MessageAssembler<S>
{
	/**
	 * Assembles the {@link Message} to be sent to Kafka.
	 *
	 * @param key    the key to be used for the message
	 * @param tenant the tenant for which the message should be assembled
	 * @param source the source object on which the {@link Message} should be based on
	 * @return the assembled {@link Message}
	 */
	Message<S> assemble(String key, String tenant, S source);

	/**
	 * Assembles the {@link Message} to be sent to Kafka.
	 *
	 * @param key    the key to be used for the message
	 * @param tenant the tenant for which the message should be assembled
	 * @param source the source object on which the {@link Message} should be based on
	 * @param locale the locale for the localizable fields from {@code source} object
	 * @return the assembled {@link Message}
	 */
	Message<S> assemble(String key, String tenant, S source, Locale locale);

	/**
	 * Assembles the {@link Message} to be sent to Kafka with the given headers.
	 *
	 * @param key     the key to be used for the message
	 * @param tenant  the tenant for which the message should be assembled
	 * @param source  the source object on which the {@link Message} should be based on
	 * @param headers the header object on which will be added to the {@link Message}
	 * @return the assembled {@link Message} with the given headers
	 * @throws {@link IllegalArgumentException} if headers include an override value for tenant or key
	 */
	Message<S> assemble(String key, String tenant, S source, Map<String, Object> headers);
}
