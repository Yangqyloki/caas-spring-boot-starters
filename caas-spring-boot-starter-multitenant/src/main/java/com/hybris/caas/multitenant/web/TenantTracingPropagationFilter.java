package com.hybris.caas.multitenant.web;

import brave.Tracing;
import brave.baggage.BaggageField;
import brave.propagation.TraceContext;
import com.hybris.caas.multitenant.service.TenantService;
import com.hybris.caas.multitenant.service.exception.InvalidTenantException;
import com.hybris.caas.multitenant.service.exception.MissingTenantException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Servlet filter to inject the "tenant" into the tracing context if the tenant was found.
 */
public class TenantTracingPropagationFilter extends OncePerRequestFilter
{
	private static final Logger LOG = LoggerFactory.getLogger(TenantTracingPropagationFilter.class);

	static final String TENANT_KEY = "tenant_id";
	static final String INVALID_MISSING_TENANT_LOG = "Missing or invalid tenant information extracted from request";
	private final Tracing tracing;
	private final TenantService tenantService;

	public TenantTracingPropagationFilter(final Tracing tracing, final TenantService tenantService)
	{
		this.tracing = tracing;
		this.tenantService = tenantService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException
	{
		final TraceContext traceContext = tracing.tracer().currentSpan().context();
		try
		{
			Optional.ofNullable(tenantService.getTenant(request))
					.ifPresent(tenant -> setInTraceContext(traceContext, TENANT_KEY, tenant));
		}
		catch (final MissingTenantException | InvalidTenantException e)
		{
			LOG.debug(INVALID_MISSING_TENANT_LOG, e);
		}
		filterChain.doFilter(request, response);
		MDC.remove(TENANT_KEY);
	}

	void setInTraceContext(final TraceContext traceContext, final String key, final String value)
	{
		BaggageField.getByName(traceContext, key).updateValue(traceContext, value);
		MDC.put(key, value);
	}
}
