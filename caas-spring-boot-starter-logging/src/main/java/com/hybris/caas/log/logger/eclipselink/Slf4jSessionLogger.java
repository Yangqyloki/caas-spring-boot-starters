package com.hybris.caas.log.logger.eclipselink;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EclipseLink Slf4j session logger will translate the log levels to slf4j and
 * log the messages and exceptions.
 */
@SuppressWarnings("squid:S1312")
public class Slf4jSessionLogger extends AbstractSessionLog
{
	public static final String ECLIPSELINK_NAMESPACE = "org.eclipse.persistence.logging";
	public static final String DEFAULT_CATEGORY = "default";
	public static final String DEFAULT_ECLIPSELINK_NAMESPACE = ECLIPSELINK_NAMESPACE + "." + DEFAULT_CATEGORY;

	private Map<String, Logger> categoryLoggers = new HashMap<>();

	public Slf4jSessionLogger()
	{
		for (final String category : SessionLog.loggerCatagories)
		{
			categoryLoggers.put(category, LoggerFactory.getLogger(ECLIPSELINK_NAMESPACE + "." + category));
		}
		categoryLoggers.put(DEFAULT_CATEGORY, LoggerFactory.getLogger(DEFAULT_ECLIPSELINK_NAMESPACE));
	}

	@Override
	public void log(SessionLogEntry entry)
	{
		if (shouldLog(entry.getLevel(), entry.getNameSpace()))
		{
			final Logger logger = getLogger(entry.getNameSpace());
			final LogLevel logLevel = toLogLevel(entry.getLevel());
			final String message = buildMessage(entry);

			switch (logLevel)
			{
			case TRACE:
				logger.trace(message);
				break;
			case DEBUG:
				logger.debug(message);
				break;
			case INFO:
				logger.info(message);
				break;
			case WARN:
				logger.warn(message);
				break;
			case ERROR:
				logger.error(message);
				break;
			default:
				break;
			}
		}
	}

	protected String buildMessage(SessionLogEntry entry)
	{
		final StringBuilder message = new StringBuilder().append(getSupplementDetailString(entry)).append(formatMessage(entry));
		if (entry.hasException())
		{
			message.append(entry.getException());
		}
		return message.toString();
	}

	@Override
	public boolean shouldLog(int level, String category)
	{
		final Logger logger = getLogger(category);
		switch (toLogLevel(level))
		{
		case TRACE:
			return logger.isTraceEnabled();
		case DEBUG:
			return logger.isDebugEnabled();
		case INFO:
			return logger.isInfoEnabled();
		case WARN:
			return logger.isWarnEnabled();
		case ERROR:
			return logger.isErrorEnabled();
		default:
			return false;
		}
	}

	@Override
	public boolean shouldLog(int level)
	{
		return shouldLog(level, DEFAULT_CATEGORY);
	}

	@Override
	public boolean shouldDisplayData()
	{
		return this.shouldDisplayData != null && shouldDisplayData;
	}

	protected Logger getLogger(String category)
	{
		return categoryLoggers.getOrDefault(category, categoryLoggers.get(DEFAULT_CATEGORY));
	}

	/**
	 * Convert from eclipselink log level to slf4j log level.
	 *
	 * @param level
	 *            the eclipselink log level
	 * @return the slf4j log level
	 */
	private static LogLevel toLogLevel(int level)
	{
		switch (level)
		{
		case SessionLog.ALL:
			return LogLevel.TRACE;
		case SessionLog.FINEST:
			return LogLevel.TRACE;
		case SessionLog.FINER:
			return LogLevel.TRACE;
		case SessionLog.FINE:
			return LogLevel.DEBUG;
		case SessionLog.CONFIG:
			return LogLevel.DEBUG;
		case SessionLog.INFO:
			return LogLevel.INFO;
		case SessionLog.WARNING:
			return LogLevel.WARN;
		case SessionLog.SEVERE:
			return LogLevel.ERROR;
		default:
			return LogLevel.OFF;
		}
	}

	/**
	 * Log levels present in slf4j + OFF for do not log.
	 */
	private enum LogLevel
	{
		TRACE, DEBUG, INFO, WARN, ERROR, OFF
	}

	public Map<String, Logger> getCategoryLoggers()
	{
		return categoryLoggers;
	}

	public void setCategoryLoggers(final Map<String, Logger> categoryLoggers)
	{
		this.categoryLoggers = categoryLoggers;
	}

}