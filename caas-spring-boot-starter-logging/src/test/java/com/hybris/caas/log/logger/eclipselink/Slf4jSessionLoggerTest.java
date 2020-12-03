package com.hybris.caas.log.logger.eclipselink;

import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * This test uses an internal logger that is bound to the logback.xml in
 * src/test/resources. Do not change the log level in that file.
 */
@RunWith(MockitoJUnitRunner.class)
public class Slf4jSessionLoggerTest
{
	private static final String TEST_EXCEPTION = "TEST EXCEPTION";

	private static final String TESTING_MESSAGE = "testing message";

	private Slf4jSessionLogger slf4jSessionLogger;

	@Mock
	private Logger logger;
	@Mock
	private AbstractSession session;

	private Map<String, Logger> mockedLoggers;

	@Before
	public void setUp()
	{
		slf4jSessionLogger = new Slf4jSessionLogger();

		mockedLoggers = new HashMap<>();
		mockedLoggers.put("default", logger);

	}

	@Test
	public void should_not_log_off()
	{
		assertFalse(slf4jSessionLogger.shouldLog(SessionLog.OFF));
	}

	@Test
	public void should_not_log_all()
	{
		assertFalse(slf4jSessionLogger.shouldLog(SessionLog.ALL));
	}

	@Test
	public void should_not_log_finest()
	{
		assertFalse(slf4jSessionLogger.shouldLog(SessionLog.FINEST));
	}

	@Test
	public void should_not_log_finer()
	{
		assertFalse(slf4jSessionLogger.shouldLog(SessionLog.FINER));
	}

	@Test
	public void should_not_log_fine()
	{
		assertFalse(slf4jSessionLogger.shouldLog(SessionLog.FINE));
	}

	@Test
	public void should_not_log_config()
	{
		assertFalse(slf4jSessionLogger.shouldLog(SessionLog.CONFIG));
	}

	@Test
	public void should_log_info()
	{
		assertTrue(slf4jSessionLogger.shouldLog(SessionLog.INFO));
	}

	@Test
	public void should_log_warning()
	{
		assertTrue(slf4jSessionLogger.shouldLog(SessionLog.WARNING));
	}

	@Test
	public void should_log_severe()
	{
		assertTrue(slf4jSessionLogger.shouldLog(SessionLog.SEVERE));
	}

	@Test
	public void should_build_message_without_exception()
	{
		final SessionLogEntry entry = new SessionLogEntry(session, TESTING_MESSAGE);

		assertThat(slf4jSessionLogger.buildMessage(entry), containsString(TESTING_MESSAGE));
	}

	@Test
	public void should_build_message_with_exception()
	{
		final SessionLogEntry entry = new SessionLogEntry(session, TESTING_MESSAGE);
		entry.setException(new IllegalArgumentException(TEST_EXCEPTION));

		assertThat(slf4jSessionLogger.buildMessage(entry), containsString(TEST_EXCEPTION));
	}

	@Test
	public void should_log_all_to_trace()
	{
		slf4jSessionLogger = spy(new Slf4jSessionLogger());
		slf4jSessionLogger.setCategoryLoggers(mockedLoggers);

		final SessionLogEntry entry = new SessionLogEntry(session, TESTING_MESSAGE);
		entry.setLevel(SessionLog.ALL);

		when(logger.isTraceEnabled()).thenReturn(Boolean.TRUE);
		doReturn(TESTING_MESSAGE).when(slf4jSessionLogger).buildMessage(entry);

		slf4jSessionLogger.log(entry);

		verify(logger).trace(TESTING_MESSAGE);
	}

	@Test
	public void should_log_finest_to_trace()
	{
		slf4jSessionLogger = spy(new Slf4jSessionLogger());
		slf4jSessionLogger.setCategoryLoggers(mockedLoggers);

		final SessionLogEntry entry = new SessionLogEntry(session, TESTING_MESSAGE);
		entry.setLevel(SessionLog.FINEST);

		when(logger.isTraceEnabled()).thenReturn(Boolean.TRUE);
		doReturn(TESTING_MESSAGE).when(slf4jSessionLogger).buildMessage(entry);

		slf4jSessionLogger.log(entry);

		verify(logger).trace(TESTING_MESSAGE);
	}

	@Test
	public void should_log_finer_to_trace()
	{
		slf4jSessionLogger = spy(new Slf4jSessionLogger());
		slf4jSessionLogger.setCategoryLoggers(mockedLoggers);

		final SessionLogEntry entry = new SessionLogEntry(session, TESTING_MESSAGE);
		entry.setLevel(SessionLog.FINER);

		when(logger.isTraceEnabled()).thenReturn(Boolean.TRUE);
		doReturn(TESTING_MESSAGE).when(slf4jSessionLogger).buildMessage(entry);

		slf4jSessionLogger.log(entry);

		verify(logger).trace(TESTING_MESSAGE);
	}

	@Test
	public void should_log_fine_to_debug()
	{
		slf4jSessionLogger = spy(new Slf4jSessionLogger());
		slf4jSessionLogger.setCategoryLoggers(mockedLoggers);

		final SessionLogEntry entry = new SessionLogEntry(session, TESTING_MESSAGE);
		entry.setLevel(SessionLog.FINE);

		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		doReturn(TESTING_MESSAGE).when(slf4jSessionLogger).buildMessage(entry);

		slf4jSessionLogger.log(entry);

		verify(logger).debug(TESTING_MESSAGE);
	}

	@Test
	public void should_log_config_to_debug()
	{
		slf4jSessionLogger = spy(new Slf4jSessionLogger());
		slf4jSessionLogger.setCategoryLoggers(mockedLoggers);

		final SessionLogEntry entry = new SessionLogEntry(session, TESTING_MESSAGE);
		entry.setLevel(SessionLog.CONFIG);

		when(logger.isDebugEnabled()).thenReturn(Boolean.TRUE);
		doReturn(TESTING_MESSAGE).when(slf4jSessionLogger).buildMessage(entry);

		slf4jSessionLogger.log(entry);

		verify(logger).debug(TESTING_MESSAGE);
	}

	@Test
	public void should_log_info_to_info()
	{
		slf4jSessionLogger = spy(new Slf4jSessionLogger());
		slf4jSessionLogger.setCategoryLoggers(mockedLoggers);

		final SessionLogEntry entry = new SessionLogEntry(session, TESTING_MESSAGE);
		entry.setLevel(SessionLog.INFO);

		when(logger.isInfoEnabled()).thenReturn(Boolean.TRUE);
		doReturn(TESTING_MESSAGE).when(slf4jSessionLogger).buildMessage(entry);

		slf4jSessionLogger.log(entry);

		verify(logger).info(TESTING_MESSAGE);
	}

	@Test
	public void should_log_warning_to_warn()
	{
		slf4jSessionLogger = spy(new Slf4jSessionLogger());
		slf4jSessionLogger.setCategoryLoggers(mockedLoggers);

		final SessionLogEntry entry = new SessionLogEntry(session, TESTING_MESSAGE);
		entry.setLevel(SessionLog.WARNING);

		when(logger.isWarnEnabled()).thenReturn(Boolean.TRUE);
		doReturn(TESTING_MESSAGE).when(slf4jSessionLogger).buildMessage(entry);

		slf4jSessionLogger.log(entry);

		verify(logger).warn(TESTING_MESSAGE);
	}

	@Test
	public void should_log_severe_to_error()
	{
		slf4jSessionLogger = spy(new Slf4jSessionLogger());
		slf4jSessionLogger.setCategoryLoggers(mockedLoggers);

		final SessionLogEntry entry = new SessionLogEntry(session, TESTING_MESSAGE);
		entry.setLevel(SessionLog.SEVERE);

		when(logger.isErrorEnabled()).thenReturn(Boolean.TRUE);
		doReturn(TESTING_MESSAGE).when(slf4jSessionLogger).buildMessage(entry);

		slf4jSessionLogger.log(entry);

		verify(logger).error(TESTING_MESSAGE);
	}

	@Test
	public void should_not_log_off_anywhere()
	{
		slf4jSessionLogger = spy(new Slf4jSessionLogger());
		slf4jSessionLogger.setCategoryLoggers(mockedLoggers);

		final SessionLogEntry entry = new SessionLogEntry(session, TESTING_MESSAGE);
		entry.setLevel(SessionLog.OFF);

		slf4jSessionLogger.log(entry);

		verifyNoInteractions(logger);
	}

}
