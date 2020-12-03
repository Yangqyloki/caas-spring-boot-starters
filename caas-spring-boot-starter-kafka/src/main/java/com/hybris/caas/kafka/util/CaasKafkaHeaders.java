package com.hybris.caas.kafka.util;

/**
 * Defines constants for the names of the Kafka headers.
 */
public final class CaasKafkaHeaders
{
	/**
	 * Identifier of the header holding the tenant.
	 */
	public static final String TENANT = "tenant";

	/**
	 * Identifier of the header holding the message identifier.
	 */
	public static final String MESSAGE_ID = "messageId";

	/**
	 * Identifier of the header holding the correlation identifier.
	 */
	public static final String CORRELATION_ID = "correlationId";

	/**
	 * Identifier of the header holding locale of the localizable fields.
	 */
	public static final String CONTENT_LANGUAGE = "content_language";

	private CaasKafkaHeaders()
	{
		// private constructor
	}
}

