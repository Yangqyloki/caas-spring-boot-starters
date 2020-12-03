package com.hybris.caas.error.converter.spring.db.eclipselink;

import com.hybris.caas.error.db.ExceptionToMessageMapper;
import org.postgresql.util.PSQLException;
import org.springframework.transaction.HeuristicCompletionException;
import org.springframework.transaction.TransactionSystemException;

/**
 * Converts {@link TransactionSystemException} wrapped in a {@link HeuristicCompletionException} to canonical error message.
 */
public class HeuristicCompletionExceptionConverter extends AbstractDatasourceExceptionConverter<HeuristicCompletionException>
{
	public HeuristicCompletionExceptionConverter(final ExceptionToMessageMapper<PSQLException> exceptionToMessageMapper)
	{
		super(exceptionToMessageMapper);
	}
}
