package com.hybris.caas.security.web;

import brave.Tracing;
import brave.propagation.StrictScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.propagation.TraceContext;
import com.sap.cloud.security.xsuaa.token.Token;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import static com.hybris.caas.security.web.UserTracingPropagationFilter.IP_KEY;
import static com.hybris.caas.security.web.UserTracingPropagationFilter.SUBACCOUNT_ID_KEY;
import static com.hybris.caas.security.web.UserTracingPropagationFilter.USER_KEY;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserTracingPropagationFilterTest
{
	private static final String ACCOUNT_ID = "account-id";
	private static final String IP_ADDRESS = "1.2.3.4";
	private static final String SUBACCOUNT_ID = UUID.randomUUID().toString();

	private UserTracingPropagationFilter filter;

	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain filterChain;
	@Mock
	private Token token;

	private TraceContext traceContext;
	private final String[] accountIds = {ACCOUNT_ID};

	@Before
	public void setUp()
	{
		final Tracing tracing = Tracing.newBuilder()
				.localServiceName("client")
				.currentTraceContext(
						ThreadLocalCurrentTraceContext.newBuilder().addScopeDecorator(StrictScopeDecorator.create()).build())
				.build();
		traceContext = tracing.tracer().startScopedSpan("my-span").context();

		filter = spy(new UserTracingPropagationFilter(tracing));

		when(request.getHeader(UserTracingPropagationFilter.SAP_UPSCALE_SUBACCOUNTID)).thenReturn(null);
		doReturn(token).when(filter).getToken();
		doReturn(accountIds).when(filter).getUserAttribute(token);
		doNothing().when(filter).setInTraceContext(traceContext, SUBACCOUNT_ID_KEY, SUBACCOUNT_ID);
		doNothing().when(filter).setInTraceContext(traceContext, USER_KEY, ACCOUNT_ID);
		doNothing().when(filter).setInTraceContext(traceContext, IP_KEY, IP_ADDRESS);
	}

	@Test
	public void should_set_accountId_in_trace_context() throws ServletException, IOException
	{
		filter.doFilterInternal(request, response, filterChain);
		verify(filter).setInTraceContext(traceContext, USER_KEY, ACCOUNT_ID);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_not_set_accountId_in_trace_context_when_null_UserInfo() throws ServletException, IOException
	{
		doReturn(null).when(filter).getToken();
		filter.doFilterInternal(request, response, filterChain);
		verify(filter, times(0)).setInTraceContext(traceContext, USER_KEY, ACCOUNT_ID);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_not_set_accountId_in_trace_context_when_null_accountIds() throws ServletException, IOException
	{
		doReturn(null).when(filter).getUserAttribute(token);
		filter.doFilterInternal(request, response, filterChain);
		verify(filter, times(0)).setInTraceContext(traceContext, USER_KEY, ACCOUNT_ID);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_not_set_accountId_in_trace_context_when_empty_accountIds() throws ServletException, IOException
	{
		doReturn(new String[] {}).when(filter).getUserAttribute(token);
		filter.doFilterInternal(request, response, filterChain);
		verify(filter, times(0)).setInTraceContext(traceContext, USER_KEY, ACCOUNT_ID);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_set_clientIP_in_trace_context() throws ServletException, IOException
	{
		doReturn(IP_ADDRESS).when(request).getRemoteAddr();
		filter.doFilterInternal(request, response, filterChain);
		verify(filter).setInTraceContext(traceContext, IP_KEY, IP_ADDRESS);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_set_first_clientIP_in_trace_context() throws ServletException, IOException
	{
		doReturn(IP_ADDRESS).when(request).getRemoteAddr();
		filter.doFilterInternal(request, response, filterChain);
		verify(filter).setInTraceContext(traceContext, IP_KEY, IP_ADDRESS);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_not_set_clientIP_in_trace_context_when_null() throws ServletException, IOException
	{
		filter.doFilterInternal(request, response, filterChain);
		verify(filter, times(0)).setInTraceContext(traceContext, IP_KEY, IP_ADDRESS);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_set_subaccountId_from_header_in_trace_context() throws ServletException, IOException
	{
		when(request.getHeader(UserTracingPropagationFilter.SAP_UPSCALE_SUBACCOUNTID)).thenReturn(SUBACCOUNT_ID);
		doReturn(null).when(filter).getSubaccountId(token);

		filter.doFilterInternal(request, response, filterChain);
		verify(filter).setInTraceContext(traceContext, SUBACCOUNT_ID_KEY, SUBACCOUNT_ID);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_set_subaccountId_from_Token_in_trace_context() throws ServletException, IOException
	{
		when(token.getSubaccountId()).thenReturn(SUBACCOUNT_ID);

		filter.doFilterInternal(request, response, filterChain);
		verify(filter).setInTraceContext(traceContext, SUBACCOUNT_ID_KEY, SUBACCOUNT_ID);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_not_set_subaccountId_in_trace_context() throws ServletException, IOException
	{
		doReturn(null).when(filter).getToken();

		filter.doFilterInternal(request, response, filterChain);
		verify(filter, times(0)).setInTraceContext(eq(traceContext), eq(SUBACCOUNT_ID_KEY), anyString());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_not_set_subaccountId_in_trace_context_when_not_in_Token() throws ServletException, IOException
	{
		when(token.getSubaccountId()).thenReturn(null);

		filter.doFilterInternal(request, response, filterChain);
		verify(filter, times(0)).setInTraceContext(eq(traceContext), eq(SUBACCOUNT_ID_KEY), anyString());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_not_set_subaccountId_in_trace_context_when_not_UUID_in_header() throws ServletException, IOException
	{
		when(request.getHeader(UserTracingPropagationFilter.SAP_UPSCALE_SUBACCOUNTID)).thenReturn("dummy");
		doReturn(null).when(filter).getSubaccountId(token);

		filter.doFilterInternal(request, response, filterChain);
		verify(filter, times(0)).setInTraceContext(eq(traceContext), eq(SUBACCOUNT_ID_KEY), anyString());
		verify(filterChain).doFilter(request, response);
	}
}
