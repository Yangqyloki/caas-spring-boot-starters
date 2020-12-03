package com.hybris.caas.kafka.message;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.hybris.caas.kafka.util.CaasKafkaHeaders.CONTENT_LANGUAGE;
import static com.hybris.caas.kafka.util.CaasKafkaHeaders.MESSAGE_ID;
import static com.hybris.caas.kafka.util.CaasKafkaHeaders.TENANT;

/**
 * Implementation of the {@link MessageAssembler} interface.
 *
 * @param <S> the type of the message and source object
 */
public class MessageAssemblerImpl<S> implements MessageAssembler<S>
{
	private final String topic;

	public MessageAssemblerImpl(final String topic)
	{
		this.topic = topic;
	}

	public String getTopic()
	{
		return topic;
	}

	@Override
	public Message<S> assemble(final String key, final String tenant, final S source)
	{
		return assembleMessage(key, tenant, source, null, Collections.emptyMap());
	}

	@Override
	public Message<S> assemble(final String key, final String tenant, final S source, final Locale locale)
	{
		Assert.notNull(locale, "locale cannot be null");

		return assembleMessage(key, tenant, source, locale, Collections.emptyMap());
	}

	@Override
	public Message<S> assemble(final String key, final String tenant, final S source, final Map<String, Object> headers)
	{
		return assembleMessage(key, tenant, source, null, headers);
	}

	private Message<S> assembleMessage(final String key, final String tenant, final S source, final Locale locale,
			final Map<String, Object> headers)
	{
		Assert.notNull(headers, "headers cannot be null");

		final MessageBuilder<S> messageBuilder = MessageBuilder.withPayload(source);

		if (Objects.nonNull(locale))
		{
			messageBuilder.setHeader(CONTENT_LANGUAGE, locale.toLanguageTag());
		}
		messageBuilder.setHeader(MESSAGE_ID, UUID.randomUUID().toString())
				.setHeader(TENANT, tenant)
				.setHeader(KafkaHeaders.TOPIC, topic)
				.setHeader(KafkaHeaders.MESSAGE_KEY, key)
				.setHeader(KafkaHeaders.TIMESTAMP, OffsetDateTime.now().toInstant().toEpochMilli());
		handleHeaders(key, tenant, headers, messageBuilder);

		return messageBuilder.build();
	}

	/**
	 * Handles the extra headers and add them to the message in the case they don't override the key or the tenant values
	 *
	 * @param key            the key to be used for the message
	 * @param tenant         the tenant for which the message should be assembled
	 * @param headers        the headers to be added to the message
	 * @param messageBuilder to be updated with extra headers
	 * @throws {@link IllegalArgumentException} if the headers map attempt to override the Tenant or Key values
	 */
	private void handleHeaders(final String key, final String tenant, final Map<String, Object> headers,
			final MessageBuilder<S> messageBuilder)
	{
		if ((headers.containsKey(TENANT) && !headers.get(TENANT).equals(tenant)) || (headers.containsKey(KafkaHeaders.MESSAGE_KEY)
				&& !headers.get(KafkaHeaders.MESSAGE_KEY).equals(key)))
		{
			throw new IllegalArgumentException("headers can't contain an override value for Key nor Tenant");
		}
		else
		{
			headers.forEach(messageBuilder::setHeader);
		}
	}
}
