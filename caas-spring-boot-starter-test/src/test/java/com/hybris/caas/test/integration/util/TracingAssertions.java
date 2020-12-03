package com.hybris.caas.test.integration.util;

import brave.Tracing;
import brave.baggage.BaggageField;
import brave.propagation.TraceContext;
import com.sap.hcp.cf.logging.common.Fields;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hybris.caas.log.tracing.Constants.CLIENT_IP;
import static com.hybris.caas.log.tracing.Constants.SUBACCOUNT_ID;
import static com.hybris.caas.log.tracing.Constants.USER_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

public final class TracingAssertions
{
	private static final Logger LOG = LoggerFactory.getLogger(TracingAssertions.class);

	public static final String IP_ADDRESSES = "10.10.10.10, 20.20.20.20";
	public static final String TENANT_VALUE = "caas";
	public static final String SUBACCOUNT_ID_VALUE = "caas-subaccount-id"; // the value set in the token for identityZoneId
	public static final String USER_ID_VALUE = "caas-test";
	public static final String CORRELATION_ID_VALUE = "7d432egh-dbf7-a738-2b77-da135733254b";
	public static final String ORGANIZATION_ID = "-";
	public static final String ORGANIZATION_NAME = "-";
	public static final String COMPONENT_TYPE = "application";

	private TracingAssertions()
	{
		// private constructor for utility class
	}

	public static void assertUserTraceContext(Tracing tracing)
	{
		final TraceContext traceContext = tracing.tracer().currentSpan().context();
		final String userId = BaggageField.getByName(traceContext, USER_ID).getValue();
		final String tenant = BaggageField.getByName(traceContext, Fields.TENANT_ID).getValue();
		final String subaccountId = BaggageField.getByName(traceContext, SUBACCOUNT_ID).getValue();
		final String clientIp = BaggageField.getByName(traceContext, CLIENT_IP).getValue();

		LOG.info("userId={}, tenant={}, subaccountId={}, clientIp={}", userId, tenant, subaccountId, clientIp);
		try
		{
			assertThat(userId, equalTo(USER_ID_VALUE));
			assertThat(tenant, equalTo(TENANT_VALUE));
			assertThat(subaccountId, equalTo(SUBACCOUNT_ID_VALUE));
			assertTrue(StringUtils.isNotBlank(clientIp));
		}
		catch (final AssertionError error)
		{
			throw new RuntimeException("Assertion Error in server processing.", error);
		}
	}

	public static void assertSpanParentId(Tracing tracing)
	{
		final TraceContext traceContext = tracing.tracer().currentSpan().context();
		try
		{
			assertTrue(traceContext.parentIdAsLong() != 0);
		}
		catch (final AssertionError error)
		{
			throw new RuntimeException("Assertion Error in server processing.", error);
		}
	}

}
