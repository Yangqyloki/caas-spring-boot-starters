package com.hybris.caas.log.tracing;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;

public class TracingUtilsTest
{
	private static final long TRACE_ID = -6148914691236517206L;

	@Test
	public void should_support_uuid_lowercase()
	{
		final long[] traceId = TracingUtils.convertUUIDStringToLong("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

		assertThat(traceId[0], equalTo(TRACE_ID));
		assertThat(traceId[1], equalTo(TRACE_ID));
	}

	@Test
	public void should_support_uuid_no_hyphens()
	{
		final long[] traceId = TracingUtils.convertUUIDStringToLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

		assertThat(traceId[0], equalTo(TRACE_ID));
		assertThat(traceId[1], equalTo(TRACE_ID));
	}

	@Test
	public void should_support_uuid_uppercase()
	{
		final long[] traceId = TracingUtils.convertUUIDStringToLong("AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA");

		assertThat(traceId[0], equalTo(TRACE_ID));
		assertThat(traceId[1], equalTo(TRACE_ID));
	}

	@Test
	public void should_fail_uuid_with_nonHex()
	{
		final long[] traceId = TracingUtils.convertUUIDStringToLong("XAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAX");
		assertThat(traceId, nullValue());
	}

	@Test
	public void should_allow_uuid_with_invalid_length_between_16_and_32_chars()
	{
		final long[] traceId = TracingUtils.convertUUIDStringToLong("AAAAAAAAAAAAAAAAAAAA");
		assertThat(traceId[0], equalTo(43690L));
		assertThat(traceId[1], equalTo(TRACE_ID));
	}

	@Test
	public void should_not_allow_uuid_with_invalid_length_less_than_16_chars()
	{
		final long[] traceId = TracingUtils.convertUUIDStringToLong("AAAAAAAAAAAAAAA");
		assertThat(traceId, nullValue());
	}

	@Test
	public void should_allow_uuid_with_invalid_length_more_than_32_chars()
	{
		final long[] traceId = TracingUtils.convertUUIDStringToLong("AAAAAAAAAAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAAAAAA");
		assertThat(traceId[0], equalTo(TRACE_ID));
		assertThat(traceId[1], equalTo(TRACE_ID));
	}

}
