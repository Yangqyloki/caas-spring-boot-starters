package com.hybris.caas.error.converter.spring.db.eclipselink;

import com.hybris.caas.error.db.ExceptionToMessageMapper;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.postgresql.util.PSQLException;
import org.springframework.transaction.TransactionSystemException;

/**
 * Converts {@link TransactionSystemException} wrapped in a {@link DatabaseException} to canonical error message.
 */
public class EclipseLinkTransactionSystemExceptionConverter extends AbstractDatasourceExceptionConverter<TransactionSystemException>
{
	public EclipseLinkTransactionSystemExceptionConverter(final ExceptionToMessageMapper<PSQLException> exceptionToMessageMapper)
	{
		super(exceptionToMessageMapper);
	}
}
