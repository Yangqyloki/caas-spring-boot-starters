package com.hybris.caas.multitenant.web;

import brave.Tracing;
import brave.propagation.StrictScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.propagation.TraceContext;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.hybris.caas.multitenant.service.TenantService;
import com.hybris.caas.multitenant.service.exception.InvalidTenantException;
import com.hybris.caas.multitenant.service.exception.MissingTenantException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.UriUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.hybris.caas.multitenant.web.TenantTracingPropagationFilter.INVALID_MISSING_TENANT_LOG;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TenantTracingPropagationFilterTest
{
	private static final Logger ROOT_LOGGER = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	private final static String UTF_8 = "UTF-8";
	private final static String REQUEST_DOMAIN = "www.mockdomain.com";
	private final static String REQUEST_URI = "/fakepath";
	private final static String REQUEST_QUERY = "param1=foo%26param2=bar";
	private final static String INVALID_REQUEST_URI = "/fakepath\n";
	private final static String INVALID_REQUEST_QUERY = "param1=foo&pa\nram2=bar";
	private static final String SAP = "sap";

	private TenantTracingPropagationFilter filter;

	@Mock
	private TenantService tenantService;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain filterChain;
	@Mock
	private Appender<ILoggingEvent> mockAppender;
	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

	private TraceContext traceContext;
	private MockHttpServletRequest request;

	@Before
	public void setUp()
	{
		final Tracing tracing = Tracing.newBuilder()
				.localServiceName("client")
				.currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
						.addScopeDecorator(StrictScopeDecorator.create()).build())
				.build();
		traceContext = tracing.tracer().startScopedSpan("my-span").context();

		request = new MockHttpServletRequest();

		filter = spy(new TenantTracingPropagationFilter(tracing, tenantService));

		ROOT_LOGGER.addAppender(mockAppender);
		ROOT_LOGGER.setLevel(Level.DEBUG);

		when(tenantService.getTenant(request)).thenReturn(SAP);
		doNothing().when(filter).setInTraceContext(traceContext, TenantTracingPropagationFilter.TENANT_KEY, SAP);
	}

	@After
	public void cleanup()
	{
		ROOT_LOGGER.detachAppender(mockAppender);
	}

	@Test
	public void should_set_tenant_in_trace_context() throws ServletException, IOException
	{
		filter.doFilterInternal(request, response, filterChain);
		verify(filter).setInTraceContext(traceContext, TenantTracingPropagationFilter.TENANT_KEY, SAP);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_not_set_tenant_in_trace_context_when_null() throws ServletException, IOException
	{
		when(tenantService.getTenant(request)).thenReturn(null);
		filter.doFilterInternal(request, response, filterChain);
		verify(filter, times(0)).setInTraceContext(traceContext, TenantTracingPropagationFilter.TENANT_KEY, SAP);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_not_set_tenant_in_trace_context_when_MissingTenantException() throws ServletException, IOException
	{
		when(tenantService.getTenant(request)).thenThrow(MissingTenantException.class);
		filter.doFilterInternal(request, response, filterChain);
		verify(filter, times(0)).setInTraceContext(traceContext, TenantTracingPropagationFilter.TENANT_KEY, SAP);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_not_set_tenant_in_trace_context_when_InvalidTenantException() throws ServletException, IOException
	{
		when(tenantService.getTenant(request)).thenThrow(InvalidTenantException.class);

		filter.doFilterInternal(request, response, filterChain);

		verify(filter, times(0)).setInTraceContext(traceContext, TenantTracingPropagationFilter.TENANT_KEY, SAP);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	public void should_log_invalid_tenant_message_when_InvalidTenantException() throws ServletException, IOException
	{
		when(tenantService.getTenant(request)).thenThrow(InvalidTenantException.class);
		request.setServerName(REQUEST_DOMAIN);
		request.setRequestURI(REQUEST_URI);
		request.setQueryString(REQUEST_QUERY);

		filter.doFilterInternal(request, response, filterChain);

		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		LoggingEvent event = captorLoggingEvent.getValue();

		assertThat(event.getLevel(), is(Level.DEBUG));
		assertThat(event.getFormattedMessage(), is(INVALID_MISSING_TENANT_LOG));
	}

	@Test
	public void should_log_invalid_tenant_message_when_MissingTenantException() throws ServletException, IOException
	{
		when(tenantService.getTenant(request)).thenThrow(MissingTenantException.class);
		request.setServerName(UriUtils.encode(REQUEST_DOMAIN, UTF_8));
		request.setRequestURI(UriUtils.encodePath(INVALID_REQUEST_URI, UTF_8));
		request.setQueryString(UriUtils.encodeQueryParam(INVALID_REQUEST_QUERY, UTF_8));

		filter.doFilterInternal(request, response, filterChain);

		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		LoggingEvent event = captorLoggingEvent.getValue();

		assertThat(event.getLevel(), is(Level.DEBUG));
		assertThat(event.getFormattedMessage(), is(INVALID_MISSING_TENANT_LOG));
	}
}
