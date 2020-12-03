package com.hybris.caas.log.tracing;

import com.sap.hcp.cf.logging.common.request.HttpHeaders;

/**
 * Tracing related constants.
 */
public class Constants
{
	private Constants()
	{
		// private constructor
	}

	public static final String UNKNOWN_VALUE = "-";

	@SuppressWarnings("squid:S1313")
	public static final String UNKNOWN_CLIENT_IP = "0.0.0.0";

	public static final String USER_ID = "user_id";
	public static final String CLIENT_IP = "client_ip";
	public static final String SUBACCOUNT_ID = "subaccount_id";
	public static final String TENANT = HttpHeaders.TENANT_ID.getField();

	public static final String X_B3_TRACE_ID = "X-B3-TraceId";
	public static final String X_B3_SPAN_ID = "X-B3-SpanId";
	public static final String X_B3_PARENT_SPAN_ID = "X-B3-ParentSpanId";
}
