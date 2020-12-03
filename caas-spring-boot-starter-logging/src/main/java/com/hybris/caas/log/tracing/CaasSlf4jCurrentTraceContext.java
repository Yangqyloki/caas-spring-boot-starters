package com.hybris.caas.log.tracing;

import brave.baggage.BaggageField;
import brave.internal.Nullable;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import com.sap.hcp.cf.logging.common.LogContext;
import org.slf4j.MDC;

import java.util.Optional;

import static com.sap.hcp.cf.logging.common.Fields.TENANT_ID;
import static com.sap.hcp.cf.logging.common.request.HttpHeaders.CORRELATION_ID;

/**
 * Trace context that loads sleuth <code>traceId</code> tracing identifier into the Slf4j MDC.
 * This context will also load SAP logging fields into the Slf4j MDC along with the <code>tenant</code> information.
 *
 * @see CurrentTraceContext
 * @see CurrentTraceContext.ScopeDecorator
 * @see LogContext#loadContextFields
 */
public final class CaasSlf4jCurrentTraceContext implements CurrentTraceContext.ScopeDecorator
{
	@Override
	public CurrentTraceContext.Scope decorateScope(final TraceContext currentSpan, final CurrentTraceContext.Scope scope)
	{
		// Load traces into MDC if current span is not null
		if (currentSpan != null)
		{
			// Load context fields into MDC, except for correlation_id.
			LogContext.loadContextFields();

			Optional.ofNullable(BaggageField.getByName(currentSpan, CORRELATION_ID.getName()))
					.map(field -> field.getValue(currentSpan))
					.ifPresent(value -> MDC.put(CORRELATION_ID.getField(), value));

			// Load tenant into MDC
			Optional.ofNullable(BaggageField.getByName(currentSpan, TENANT_ID))
					.map(field -> field.getValue(currentSpan))
					.ifPresent(tenant -> MDC.put(TENANT_ID, tenant));
		}
		// Remove traces from MDC if current span is null
		else
		{
			MDC.remove(CORRELATION_ID.getField());
			MDC.remove(TENANT_ID);
		}

		// Replace old traces on close
		class ThreadContextCurrentTraceContextScope implements CurrentTraceContext.Scope
		{
			@Override
			public void close()
			{
				scope.close();
				replace(TENANT_ID, null);
			}
		}
		return new ThreadContextCurrentTraceContextScope();
	}

	static void replace(String key, @Nullable String value)
	{
		if (value != null)
		{
			MDC.put(key, value);
		}
		else
		{
			MDC.remove(key);
		}
	}
}
