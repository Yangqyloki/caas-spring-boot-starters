package com.hybris.caas.kafka.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.Locale;
import java.util.Map;

import static com.hybris.caas.kafka.util.CaasKafkaHeaders.CONTENT_LANGUAGE;
import static com.hybris.caas.kafka.util.CaasKafkaHeaders.MESSAGE_ID;
import static com.hybris.caas.kafka.util.CaasKafkaHeaders.TENANT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class MessageAssemblerImplTest
{
	private static final String TOPIC = "topic";
	private static final String KEY = "key";
	private static final String TENANT_ID = "tenant";
	private static final String PAYLOAD = "payload";
	private static final String NEW_TENANT_ID = "newTenant";
	private static final String NEW_HEADER_KEY = "newHeaderKey1";
	private static final String NEW_HEADER_VALUE = "newHeaderValue1";

	private final MessageAssemblerImpl<String> messageAssembler = new MessageAssemblerImpl<>(TOPIC);

	@Test
	public void headersAndPayloadExistInMessage()
	{
		//When
		final Message<String> message = messageAssembler.assemble(KEY, TENANT_ID, PAYLOAD);

		//Then
		assertMessage(message, false);
	}

	@Test
	public void headersAndPayloadInMessageWithLocale()
	{
		//When
		final Message<String> message = messageAssembler.assemble(KEY, TENANT_ID, PAYLOAD, Locale.US);

		//Then
		assertMessage(message, true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwIllegalArgumentExceptionWhenLocaleIsNull()
	{
		//When
		final Locale locale = null;
		final Message<String> message = messageAssembler.assemble(KEY, TENANT_ID, PAYLOAD, locale);
	}

	@Test
	public void shouldAssembleMessageWithHeaders()
	{
		//given
		final Map<String, Object> headers = Map.of(NEW_HEADER_KEY, NEW_HEADER_VALUE);
		//When
		final Message<String> message = messageAssembler.assemble(KEY, TENANT_ID, PAYLOAD, headers);

		//Then
		assertMessage(message, false);
		assertTrue(headers.containsKey(NEW_HEADER_KEY));
		assertTrue(headers.containsValue(NEW_HEADER_VALUE));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotAssembleMessageWithOverrideTenantHeader()
	{
		//given
		final Map<String, Object> headers = Map.of(TENANT, NEW_TENANT_ID);
		//When
		messageAssembler.assemble(KEY, TENANT_ID, PAYLOAD, headers);
	}

	@Test
	public void shouldAssembleMessageWithSameTenantHeader()
	{
		//given
		final Map<String, Object> headers = Map.of(TENANT, TENANT_ID);
		//When
		final Message<String> message = messageAssembler.assemble(KEY, TENANT_ID, PAYLOAD, headers);
		//Then
		assertEquals(TENANT_ID,message.getHeaders().get(TENANT));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotAssembleMessageWithOverrideKeyHeader()
	{
		//given
		final Map<String, Object> headers = Map.of(KafkaHeaders.MESSAGE_KEY, NEW_HEADER_VALUE);
		//When
		messageAssembler.assemble(KEY, TENANT_ID, PAYLOAD, headers);
	}

	@Test
	public void shouldAssembleMessageWithSameKeytHeader()
	{
		//given
		final Map<String, Object> headers = Map.of(KafkaHeaders.MESSAGE_KEY, KEY);
		//When
		final Message<String> message = messageAssembler.assemble(KEY, TENANT_ID, PAYLOAD, headers);
		//Then
		assertEquals(TENANT_ID,message.getHeaders().get(TENANT));
	}
	private void assertMessage(final Message<String> message, final boolean nonNullLocaleHeader)
	{
		final MessageHeaders headers = message.getHeaders();

		//Then
		assertEquals(PAYLOAD,message.getPayload());
		assertTrue(headers.containsKey(MESSAGE_ID));
		assertEquals(TENANT_ID,headers.get(TENANT));
		assertEquals(TOPIC,headers.get(KafkaHeaders.TOPIC));
		assertEquals(KEY,headers.get(KafkaHeaders.MESSAGE_KEY));
		assertTrue(headers.containsKey(KafkaHeaders.TIMESTAMP));
		assertEquals(headers.get(CONTENT_LANGUAGE), nonNullLocaleHeader ? "en-US" : null);
	}
}
