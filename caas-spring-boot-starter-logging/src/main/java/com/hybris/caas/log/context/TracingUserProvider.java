package com.hybris.caas.log.context;

import brave.Span;
import brave.Tracing;
import brave.baggage.BaggageField;
import brave.propagation.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.hybris.caas.log.tracing.Constants.CLIENT_IP;
import static com.hybris.caas.log.tracing.Constants.SUBACCOUNT_ID;
import static com.hybris.caas.log.tracing.Constants.UNKNOWN_CLIENT_IP;
import static com.hybris.caas.log.tracing.Constants.UNKNOWN_VALUE;
import static com.hybris.caas.log.tracing.Constants.USER_ID;
import static com.sap.hcp.cf.logging.common.Fields.TENANT_ID;

/**
 * Tracing based implementation of {@link UserProvider}.
 */
public class TracingUserProvider implements UserProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(TracingUserProvider.class);
	public static final String SPAN_INFO = "span";
	private static final String TRACE_CONTEXT = "trace context";

	private final Tracing tracing;

	public TracingUserProvider(final Tracing tracing)
	{
		this.tracing = tracing;
	}

	@Override
	public String getUserId()
	{
		return getTracingField(USER_ID);
	}

	@Override
	public String getTenant()
	{
		return getTracingField(TENANT_ID);
	}

	@Override
	public String getSubaccountId()
	{
		return getTracingField(SUBACCOUNT_ID);
	}

	@Override
	public String getClientIp()
	{
		final String clientId = getTracingField(CLIENT_IP);
		return clientId.equals(UNKNOWN_VALUE) ? UNKNOWN_CLIENT_IP : clientId;
	}

	private String getTracingField(final String fieldName)
	{
		final Optional<Span> optionalSpan = Optional.ofNullable(tracing.tracer().currentSpan());
		if (!optionalSpan.isPresent())
		{
			return handleMissingInfo(SPAN_INFO, fieldName);
		}

		final Optional<TraceContext> optionalTraceContext = optionalSpan.map(Span::context);
		if (!optionalTraceContext.isPresent())
		{
			return handleMissingInfo(TRACE_CONTEXT, fieldName);
		}

		final Optional<String> fieldValue = Optional.ofNullable(BaggageField.getByName(optionalTraceContext.get(), fieldName).getValue());
		return fieldValue.orElseGet(() -> handleMissingInfo(fieldName, fieldName));
	}

	private String handleMissingInfo(final String info, final String fieldName)
	{
		LOG.info("{} not available for thread {}. Searched field: {}", info, Thread.currentThread().getName(), fieldName);
		return UNKNOWN_VALUE;
	}
}
