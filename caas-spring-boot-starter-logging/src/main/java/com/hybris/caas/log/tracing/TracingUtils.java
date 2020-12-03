package com.hybris.caas.log.tracing;

import static brave.internal.codec.HexCodec.lenientLowerHexToUnsignedLong;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

/**
 * Utilities for working with classes in the tracing package.
 */
final class TracingUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(TracingUtils.class);

	private TracingUtils()
	{
		// private constructor
	}

	/**
	 * Convert a UUID string to 2 long values for each half of the UUID without hyphens.
	 *
	 * @param hexTraceId the UUID string
	 * @return 2 long values representing the UUID hex number
	 */
	static long[] convertUUIDStringToLong(final @NonNull String hexTraceId)
	{
		final long[] result = new long[2];
		final String lowerHexTraceId = hexTraceId.toLowerCase(Locale.ENGLISH).replaceAll("-", "");
		final int length = lowerHexTraceId.length();

		// left-most characters, if any, are the high bits
		final int traceIdIndex = Math.max(0, length - 16);

		result[0] = lenientLowerHexToUnsignedLong(lowerHexTraceId, 0, traceIdIndex);
		if (result[0] == 0)
		{
			LOG.warn("{} is not a lower hex string.", lowerHexTraceId);
			return null;
		}

		// right-most up to 16 characters are the low bits
		result[1] = lenientLowerHexToUnsignedLong(lowerHexTraceId, traceIdIndex, length);
		if (result[1] == 0)
		{
			LOG.warn("{} is not a lower hex string.", lowerHexTraceId);
			return null;
		}
		return result;

	}
}
