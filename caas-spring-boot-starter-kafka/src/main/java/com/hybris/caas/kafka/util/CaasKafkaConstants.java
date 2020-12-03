package com.hybris.caas.kafka.util;

/**
 * Constants utility class to hold constants that span Kafka messages processing.
 */
public final class CaasKafkaConstants
{
	public static final String DOT_SEPARATOR = ".";

	/**
	 * Suffix used to name short delay retry topic.
	 */
	public static final String SHORT_DELAY_RETRY_TOPIC_SUFFIX = DOT_SEPARATOR + "SDR";

	/**
	 * Suffix used to name long delay retry topic.
	 */
	public static final String LONG_DELAY_RETRY_TOPIC_SUFFIX = DOT_SEPARATOR + "LDR";

	/**
	 * Suffix used to name dead letter topic.
	 */
	public static final String DEAD_LETTER_TOPIC_SUFFIX = DOT_SEPARATOR + "DLT";

	/**
	 * The sleep interval in milliseconds between retry topic polling cycles for retryable consumer.
	 * Applied only when the message delay required relative to the message processing current time is larger than this value.
	 */
	public static final int IDLE_BETWEEN_RETRY_POLLS_MS = 5_000;

	private CaasKafkaConstants()
	{
		// private constructor
	}
}
