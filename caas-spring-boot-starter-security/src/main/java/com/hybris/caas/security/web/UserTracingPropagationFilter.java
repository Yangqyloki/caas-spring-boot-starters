package com.hybris.caas.security.web;

import brave.Tracing;
import brave.baggage.BaggageField;
import brave.propagation.TraceContext;
import com.sap.cloud.security.xsuaa.token.SpringSecurityContext;
import com.sap.cloud.security.xsuaa.token.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Servlet filter to inject the "user_id" and "client_ip" into the tracing context if the data is available.
 */
public class UserTracingPropagationFilter extends OncePerRequestFilter
{
	private static final Logger LOG = LoggerFactory.getLogger(UserTracingPropagationFilter.class);

	private static final String IDENTIFIER_REG_EXP = "^[0-9A-Fa-f]{8}-([0-9A-Fa-f]{4}-){3}[0-9A-Fa-f]{12}$";

	static final String USER_KEY = "user_id";
	static final String IP_KEY = "client_ip";
	static final String SUBACCOUNT_ID_KEY = "subaccount_id";
	static final String ACCOUNT_ID = "accountId";
	static final String SAP_UPSCALE_SUBACCOUNTID = "sap-upscale-subaccountid";

	private final Tracing tracing;
	private final Pattern pattern = Pattern.compile(IDENTIFIER_REG_EXP);

	public UserTracingPropagationFilter(final Tracing tracing)
	{
		this.tracing = tracing;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException
	{
		final TraceContext traceContext = tracing.tracer().currentSpan().context();
		getUserIdFromToken().ifPresent(userId -> setInTraceContext(traceContext, USER_KEY, userId));

		final Optional<String> optionalSubaccountId = getSubaccountIdFromToken();
		if (optionalSubaccountId.isPresent())
		{
			setInTraceContext(traceContext, SUBACCOUNT_ID_KEY, optionalSubaccountId.get());
		}
		else
		{
			getSubaccountIdFromHeader(request).ifPresent(
					subaccountId -> setInTraceContext(traceContext, SUBACCOUNT_ID_KEY, subaccountId));
		}

		Optional.ofNullable(request.getRemoteAddr()).ifPresent(ip -> setInTraceContext(traceContext, IP_KEY, ip));
		filterChain.doFilter(request, response);
	}

	void setInTraceContext(final TraceContext traceContext, final String key, final String value)
	{
		BaggageField.getByName(traceContext, key).updateValue(traceContext, value);
	}

	Token getToken()
	{
		try
		{
			return SpringSecurityContext.getToken();
		}
		catch (final AccessDeniedException | ClassCastException e)
		{
			LOG.debug("Failed to extract user information object from security context.", e);
			return null;
		}
	}

	String[] getUserAttribute(final Token token)
	{
		return token.getXSUserAttribute(ACCOUNT_ID);
	}

	String getSubaccountId(final Token token)
	{
		return token.getSubaccountId();
	}

	protected Optional<String> getUserIdFromToken()
	{
		return Optional.ofNullable(getToken()).map(this::getUserAttribute).filter(array -> array.length > 0).map(array -> array[0]);
	}

	protected Optional<String> getSubaccountIdFromToken()
	{
		return Optional.ofNullable(getToken()).map(this::getSubaccountId);
	}

	protected Optional<String> getSubaccountIdFromHeader(final HttpServletRequest request)
	{
		// Only return a value if the header stores an UUID (in order to avoid any malicious data)
		return Optional.ofNullable(request.getHeader(SAP_UPSCALE_SUBACCOUNTID)).filter(value -> pattern.matcher(value).matches());
	}
}
