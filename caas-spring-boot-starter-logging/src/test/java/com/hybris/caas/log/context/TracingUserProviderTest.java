package com.hybris.caas.log.context;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import brave.baggage.BaggageField;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationConfig;
import brave.propagation.B3Propagation;
import brave.propagation.StrictScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import static com.hybris.caas.log.context.TracingUserProvider.SPAN_INFO;
import static com.hybris.caas.log.tracing.Constants.CLIENT_IP;
import static com.hybris.caas.log.tracing.Constants.SUBACCOUNT_ID;
import static com.hybris.caas.log.tracing.Constants.UNKNOWN_CLIENT_IP;
import static com.hybris.caas.log.tracing.Constants.UNKNOWN_VALUE;
import static com.hybris.caas.log.tracing.Constants.USER_ID;
import static com.sap.hcp.cf.logging.common.Fields.TENANT_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TracingUserProviderTest
{
	private static final Logger CAAS_LOGGER = (Logger) LoggerFactory.getLogger("com.hybris.caas");


	private final Tracing tracing = Tracing.newBuilder()
			.currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
					.addScopeDecorator(StrictScopeDecorator.create()).build())
			.propagationFactory(BaggagePropagation.newFactoryBuilder(B3Propagation.FACTORY)
					.add(BaggagePropagationConfig.SingleBaggageField.local(BaggageField.create(USER_ID)))
					.add(BaggagePropagationConfig.SingleBaggageField.local(BaggageField.create(TENANT_ID)))
					.add(BaggagePropagationConfig.SingleBaggageField.local(BaggageField.create(SUBACCOUNT_ID)))
					.add(BaggagePropagationConfig.SingleBaggageField.local(BaggageField.create(CLIENT_IP)))
					.build())
			.build();

	@Mock
	private Appender<ILoggingEvent> mockAppender;
	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;
	@Mock
	private Tracing mockTracing;
	@Mock
	private Tracer mockTracer;

	private TracingUserProvider userProvider;
	private ScopedSpan span;

	@Before
	public void setUp()
	{
		span = this.tracing.tracer().startScopedSpan("span");
		userProvider = new TracingUserProvider(tracing);

		CAAS_LOGGER.addAppender(mockAppender);
	}

	@After
	public void cleanUp()
	{
		span.finish();

		CAAS_LOGGER.detachAppender(mockAppender);
	}

	@Test
	public void should_return_unknow_when_no_span()
	{
		userProvider = new TracingUserProvider(mockTracing);
		when(mockTracing.tracer()).thenReturn(mockTracer);

		final String userId = userProvider.getUserId();

		assertThat(userId, equalTo(UNKNOWN_VALUE));
		assertLoggingEvent(SPAN_INFO, USER_ID);
	}

	@Test
	public void should_return_unknow_when_no_userId_available()
	{
		final String userId = userProvider.getUserId();

		assertThat(userId, equalTo(UNKNOWN_VALUE));
		assertLoggingEvent(USER_ID, USER_ID);
	}

	@Test
	public void should_return_unknow_when_no_tenant_available()
	{
		final String tenant = userProvider.getTenant();

		assertThat(tenant, equalTo(UNKNOWN_VALUE));
		assertLoggingEvent(TENANT_ID, TENANT_ID);
	}

	@Test
	public void should_return_unknow_when_no_subaccountId_available()
	{
		final String subaccountId = userProvider.getSubaccountId();

		assertThat(subaccountId, equalTo(UNKNOWN_VALUE));
		assertLoggingEvent(SUBACCOUNT_ID, SUBACCOUNT_ID);
	}

	@Test
	public void should_return_unknow_when_no_clientIp_available()
	{
		final String tenant = userProvider.getClientIp();

		assertThat(tenant, equalTo(UNKNOWN_CLIENT_IP));
		assertLoggingEvent(CLIENT_IP, CLIENT_IP);
	}

	@Test
	public void should_return_userId()
	{
		BaggageField.getByName(span.context(), USER_ID).updateValue(USER_ID);

		final String userId = userProvider.getUserId();

		assertThat(userId, equalTo(USER_ID));
		verifyNoInteractions(mockAppender);
	}

	@Test
	public void should_return_tenant()
	{
		BaggageField.getByName(span.context(), TENANT_ID).updateValue(TENANT_ID);

		final String tenant = userProvider.getTenant();

		assertThat(tenant, equalTo(TENANT_ID));
		verifyNoInteractions(mockAppender);
	}

	@Test
	public void should_return_subaccountId()
	{
		BaggageField.getByName(span.context(), SUBACCOUNT_ID).updateValue(SUBACCOUNT_ID);

		final String subaccountId = userProvider.getSubaccountId();

		assertThat(subaccountId, equalTo(SUBACCOUNT_ID));
		verifyNoInteractions(mockAppender);
	}

	@Test
	public void should_return_clientIp()
	{
		BaggageField.getByName(span.context(), CLIENT_IP).updateValue(CLIENT_IP);

		final String clientIp = userProvider.getClientIp();

		assertThat(clientIp, equalTo(CLIENT_IP));
		verifyNoInteractions(mockAppender);
	}

	private void assertLoggingEvent(final String info, final String fieldName)
	{
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

		MatcherAssert.assertThat(loggingEvent.getFormattedMessage(), allOf(containsString(info), containsString(fieldName)));
		MatcherAssert.assertThat(loggingEvent.getLevel(), equalTo(Level.INFO));
	}
}
