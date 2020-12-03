package com.hybris.caas.security.encode;

/**
 * Encoder to escape any control characters, single quotes (') and double quotes (").
 */
public final class QuoteEncoder
{
	/** A table of hex digits */
	private static final char[] hexDigit =
			{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private QuoteEncoder()
	{
		// private constructor
	}

	/**
	 * Encodes the given property value to ensure that all property control characters are escaped.
	 *
	 * @param input the input string
	 * @return the coded string
	 */
	@SuppressWarnings({"pmd:NcssMethodCount", "squid:MethodCyclomaticComplexity", "squid:S135"})
	public static String encode(final String input)
	{
		final int len = input.length();
		int bufLen = len * 2;
		if (bufLen < 0)
		{
			bufLen = Integer.MAX_VALUE;
		}
		final StringBuilder outBuffer = new StringBuilder(bufLen);

		for (int x = 0; x < len; x++)
		{
			final char aChar = input.charAt(x);
			// Handle common case first, selecting largest block that avoids the specials below
			if ((aChar > 61) && (aChar < 127))
			{
				if (aChar == '\\')
				{
					outBuffer.append('\\');
					outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch (aChar)
			{
			case '\t':
				outBuffer.append('\\');
				outBuffer.append('t');
				break;
			case '\n':
				outBuffer.append('\\');
				outBuffer.append('n');
				break;
			case '\r':
				outBuffer.append('\\');
				outBuffer.append('r');
				break;
			case '\f':
				outBuffer.append('\\');
				outBuffer.append('f');
				break;
			case '\'':
				outBuffer.append('\\');
				outBuffer.append('\'');
				break;
			case '"':
				outBuffer.append('\\');
				outBuffer.append(aChar);
				break;
			default:
				handleDefaultChar(outBuffer, aChar);
			}
		}
		return outBuffer.toString();
	}

	private static void handleDefaultChar(final StringBuilder outBuffer, final char aChar)
	{
		if (((aChar < 0x0020) || (aChar > 0x007e)))
		{
			outBuffer.append('\\');
			outBuffer.append('u');
			outBuffer.append(toHex((aChar >> 12) & 0xF));
			outBuffer.append(toHex((aChar >> 8) & 0xF));
			outBuffer.append(toHex((aChar >> 4) & 0xF));
			outBuffer.append(toHex(aChar & 0xF));
		}
		else
		{
			outBuffer.append(aChar);
		}
	}

	/**
	 * Convert a nibble to a hex character
	 *
	 * @param nibble
	 *            the nibble to convert.
	 */
	private static char toHex(int nibble)
	{
		return hexDigit[(nibble & 0xF)];
	}

}
